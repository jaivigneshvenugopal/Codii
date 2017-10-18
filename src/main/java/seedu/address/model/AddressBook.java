package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javafx.collections.ObservableList;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Deadline;
import seedu.address.model.person.Debt;
import seedu.address.model.person.Email;
import seedu.address.model.person.Interest;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.PostalCode;
import seedu.address.model.person.ReadOnlyPerson;
import seedu.address.model.person.UniquePersonList;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.model.tag.Tag;
import seedu.address.model.tag.UniqueTagList;
import seedu.address.model.tag.exceptions.TagNotFoundException;

/**
 * Wraps all data at the address-book level
 * Duplicates are not allowed (by .equals comparison)
 */
public class AddressBook implements ReadOnlyAddressBook {

    private final UniquePersonList persons;
    private final UniqueTagList tags;

    /*
     * The 'unusual' code block below is an non-static initialization block, sometimes used to avoid duplication
     * between constructors. See https://docs.oracle.com/javase/tutorial/java/javaOO/initial.html
     *
     * Note that non-static init blocks are not recommended to use. There are other ways to avoid duplication
     *   among constructors.
     */
    {
        persons = new UniquePersonList();
        tags = new UniqueTagList();
    }

    public AddressBook() {}

    /**
     * Creates an AddressBook using the Persons and Tags in the {@code toBeCopied}
     */
    public AddressBook(ReadOnlyAddressBook toBeCopied) {
        this();
        resetData(toBeCopied);
    }

    //// list overwrite operations

    public void setPersons(List<? extends ReadOnlyPerson> persons) throws DuplicatePersonException {
        this.persons.setPersons(persons);
    }

    public void setTags(Set<Tag> tags) {
        this.tags.setTags(tags);
    }

    /**
     * Resets the existing data of this {@code AddressBook} with {@code newData}.
     */
    public void resetData(ReadOnlyAddressBook newData) {
        requireNonNull(newData);
        try {
            setPersons(newData.getPersonList());
        } catch (DuplicatePersonException e) {
            assert false : "AddressBooks should not have duplicate persons";
        }
        setTags(new HashSet<>(newData.getTagList()));
        syncMasterTagListWith(persons);
    }

    /**
     * Returns {@UniquePersonList} of all blacklisted persons
     * in the existing data of this {@code AddressBook} with {@code newData}.
     */
    public UniquePersonList getBlacklistedPersons() {
        UniquePersonList blacklistedPersons = new UniquePersonList();
        for (Person person : persons.getInternalList()) {
            if (person.getIsBlacklisted()) {
                try {
                    blacklistedPersons.add(person);
                } catch (DuplicatePersonException e) {
                    assert false : "This is not possible as prior checks have been done";
                }
            }
        }
        return blacklistedPersons;
    }

    /**
     * Returns {@UniquePersonList} of all whitelisted persons
     * in the existing data of this {@code AddressBook} with {@code newData}.
     */
    public UniquePersonList getWhitelistedPersons() {
        UniquePersonList whitelistedPersons = new UniquePersonList();
        for (Person person : persons.getInternalList()) {
            if (person.getIsWhitelisted()) {
                try {
                    whitelistedPersons.add(person);
                } catch (DuplicatePersonException e) {
                    assert false : "This is not possible as prior checks have been done";
                }
            }
        }
        return whitelistedPersons;
    }

    //// person-level operations

    /**
     * Adds a person to the address book.
     * Also checks the new person's tags and updates {@link #tags} with any new tags found,
     * and updates the Tag objects in the person to point to those in {@link #tags}.
     *
     * @throws DuplicatePersonException if an equivalent person already exists.
     */
    public void addPerson(ReadOnlyPerson p) throws DuplicatePersonException {
        Person newPerson = new Person(p);
        syncMasterTagListWith(newPerson);
        // TODO: the tags master list will be updated even though the below line fails.
        // This can cause the tags master list to have additional tags that are not tagged to any person
        // in the person list.
        persons.add(newPerson);
    }

    /**
     * Adds a person to the blacklist in the address book.
     *
     * @throws DuplicatePersonException if an equivalent person already exists.
     */
    public void addBlacklistedPerson(ReadOnlyPerson p) throws DuplicatePersonException {
        int index;
        index = persons.getIndexOf(p);

        Person newBlacklistedPerson = new Person(p);
        newBlacklistedPerson.setIsBlacklisted(true);
        try {
            persons.remove(p);
        } catch (PersonNotFoundException e) {
            assert false : "This is not possible as prior checks have been done";
        }
        persons.add(index, newBlacklistedPerson);
    }

    /**
     * Adds a person to the whitelist in the address book.
     *
     * @throws DuplicatePersonException if an equivalent person already exists.
     */
    public void addWhitelistedPerson(ReadOnlyPerson p) throws DuplicatePersonException {
        int index;
        index = persons.getIndexOf(p);

        Person newWhitelistedPerson = new Person(p);
        newWhitelistedPerson.setIsWhitelisted(true);
        try {
            persons.remove(p);
        } catch (PersonNotFoundException e) {
            assert false : "This is not possible as prior checks have been done";
        }
        persons.add(index, newWhitelistedPerson);
    }

    /**
     * Replaces the given person {@code target} in the list with {@code editedReadOnlyPerson}.
     * {@code AddressBook}'s tag list will be updated with the tags of {@code editedReadOnlyPerson}.
     *
     * @throws DuplicatePersonException if updating the person's details causes the person to be equivalent to
     *      another existing person in the list.
     * @throws PersonNotFoundException if {@code target} could not be found in the list.
     *
     * @see #syncMasterTagListWith(Person)
     */
    public void updatePerson(ReadOnlyPerson target, ReadOnlyPerson editedReadOnlyPerson)
            throws DuplicatePersonException, PersonNotFoundException {
        requireNonNull(editedReadOnlyPerson);

        Person editedPerson = new Person(editedReadOnlyPerson);
        syncMasterTagListWith(editedPerson);
        // TODO: the tags master list will be updated even though the below line fails.
        // This can cause the tags master list to have additional tags that are not tagged to any person
        // in the person list.
        persons.setPerson(target, editedPerson);
    }

    /**
     * Ensures that every tag in this person:
     *  - exists in the master list {@link #tags}
     *  - points to a Tag object in the master list
     */
    private void syncMasterTagListWith(Person person) {
        final UniqueTagList personTags = new UniqueTagList(person.getTags());
        tags.mergeFrom(personTags);

        // Create map with values = tag object references in the master list
        // used for checking person tag references
        final Map<Tag, Tag> masterTagObjects = new HashMap<>();
        tags.forEach(tag -> masterTagObjects.put(tag, tag));

        // Rebuild the list of person tags to point to the relevant tags in the master tag list.
        final Set<Tag> correctTagReferences = new HashSet<>();
        personTags.forEach(tag -> correctTagReferences.add(masterTagObjects.get(tag)));
        person.setTags(correctTagReferences);
    }

    /**
     * Ensures that every tag in these persons:
     *  - exists in the master list {@link #tags}
     *  - points to a Tag object in the master list
     *  @see #syncMasterTagListWith(Person)
     */
    private void syncMasterTagListWith(UniquePersonList persons) {
        persons.forEach(this::syncMasterTagListWith);
    }

    /**
     * Removes {@code key} from this {@code AddressBook}.
     * @throws PersonNotFoundException if the {@code key} is not in this {@code AddressBook}.
     */
    public boolean removePerson(ReadOnlyPerson key) throws PersonNotFoundException {
        return persons.remove(key);
    }

    /**
     * Updates {@code key} to exclude {@code key} from the blacklist in this {@code AddressBook}.
     * @throws PersonNotFoundException if the {@code key} is not in this {@code AddressBook}.
     */
    public void removeBlacklistedPerson(ReadOnlyPerson key) throws PersonNotFoundException {
        int index;
        index = persons.getIndexOf(key);

        Person newBlacklistedPerson = new Person(key);
        newBlacklistedPerson.setIsBlacklisted(false);
        persons.remove(key);
        try {
            persons.add(index, newBlacklistedPerson);
        } catch (DuplicatePersonException e) {
            assert false : "This is not possible as prior checks have been done";
        }
    }

    /**
     * Updates {@code key} to exclude {@code key} from the whitelist in this {@code AddressBook}.
     * @throws PersonNotFoundException if the {@code key} is not in this {@code AddressBook}.
     */
    public void removeWhitelistedPerson(ReadOnlyPerson key) throws PersonNotFoundException {
        int index;
        index = persons.getIndexOf(key);

        Person newWhitelistedPerson = new Person(key);
        newWhitelistedPerson.setIsWhitelisted(false);
        persons.remove(key);
        try {
            persons.add(index, newWhitelistedPerson);
        } catch (DuplicatePersonException e) {
            assert false : "This is not possible as prior checks have been done";
        }
    }

    //// tag-level operations

    /**
     * Adds a {@code Tag} to the tag list.
     * @param t the tag to be added.
     * @throws UniqueTagList.DuplicateTagException if the tag already exists.
     */
    public void addTag(Tag t) throws UniqueTagList.DuplicateTagException {
        tags.add(t);
    }

    /**
     * Removes a {@code Tag} from the tag list.
     * @param t the tag to be removed.
     * @throws TagNotFoundException if the tag does not exist.
     */
    public void removeTag(Tag t) throws TagNotFoundException {
        tags.remove(t);
    }


    //@@author jelneo

    /**
     * Increase debts of a person by the indicated amount
     * @param target person that borrowed more money
     * @param amount amount that the person borrowed. Must be either a positive integer or positive number with
     *               two decimal places
     * @throws PersonNotFoundException if {@code target} could not be found in the list.
     */
    public void addDebtToPerson(ReadOnlyPerson target, Debt amount) throws PersonNotFoundException {
        Name name = target.getName();
        Phone phone = target.getPhone();
        Email email = target.getEmail();
        Address address = target.getAddress();
        PostalCode postalCode = target.getPostalCode();
        Debt newDebt = target.getDebt();
        newDebt.addToDebt(amount);
        Interest interest = target.getInterest();
        Deadline deadline = target.getDeadline();
        Set<Tag> tags = target.getTags();
        Person editedPerson = new Person(name, phone, email, address, postalCode, newDebt, interest, deadline, tags);
        editedPerson.setDateBorrow(target.getDateBorrow());
        try {
            persons.setPerson(target, editedPerson);
        } catch (DuplicatePersonException dpe) {
            assert false : "There should be no duplicate when updating the debt of a person";
        }
    }
    //@@author

    /**
     * Resets person's debt field to zero, in the mainlist of the addressbook.
     *
     *  @throws PersonNotFoundException if person does not exist in list.
     */
    public ReadOnlyPerson resetPersonDebt(ReadOnlyPerson p) throws PersonNotFoundException {
        int index;
        index = persons.getIndexOf(p);

        Person existingPerson = new Person(p);
        try {
            existingPerson.setDebt(new Debt(Debt.DEBT_ZER0_VALUE));
        } catch (IllegalValueException e) {
            assert false: "The target value cannot be of illegal value";
        }

        persons.remove(p);

        try {
            persons.add(index, existingPerson);
        } catch (DuplicatePersonException dpe) {
            assert false : "There should be no duplicate when resetting the debt of a person";
        }
        return persons.getReadOnlyPerson(index);
    }

    //// util methods

    @Override
    public String toString() {
        return persons.asObservableList().size() + " persons, "
                + tags.asObservableList().size() +  " tags";
        // TODO: refine later
    }

    @Override
    public ObservableList<ReadOnlyPerson> getPersonList() {
        return persons.asObservableList();
    }

    @Override
    public ObservableList<ReadOnlyPerson> getBlacklistedPersonList() {
        return getBlacklistedPersons().asObservableList();
    }

    @Override
    public ObservableList<ReadOnlyPerson> getWhitelistedPersonList() {
        return getWhitelistedPersons().asObservableList();
    }

    @Override
    public ObservableList<Tag> getTagList() {
        return tags.asObservableList();
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof AddressBook // instanceof handles nulls
                && this.persons.equals(((AddressBook) other).persons)
                && this.tags.equalsOrderInsensitive(((AddressBook) other).tags));
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(persons, tags);
    }
}
