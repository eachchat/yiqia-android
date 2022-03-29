package im.vector.app.yiqia.contact.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import im.vector.app.eachchat.BaseModule
import im.vector.app.yiqia.database.AppDatabase

class ContactDaoHelper constructor(private val dao: ContactDaoV2) {

    fun getContacts(): List<ContactsDisplayBean> {
        return contactV2ListToContactList(dao.getContacts())
    }

    fun insertContacts(contact: ContactsDisplayBean) {
        dao.insertContact(contact.toContactsDisplayBeanV2())
    }

    fun insertContactsV2(contact: ContactsDisplayBeanV2) {
        dao.insertContact(contact)
    }

    fun update(contact: ContactsDisplayBean): Int {
        return dao.update(contact.toContactsDisplayBeanV2())
    }

    fun updateV2(contact: ContactsDisplayBeanV2): Int {
        return dao.update(contact)
    }

    fun delete(contact: ContactsDisplayBean) {
        dao.delete(contact.toContactsDisplayBeanV2())
    }

    fun deleteContacts() {
        dao.deleteContacts()
    }

    fun getContactByMatrixId(matrixId: String): ContactsDisplayBean? {
        return dao.getContactByMatrixId(matrixId)?.toContactsDisplayBean()
    }

    fun getDelContactByMatrixId(matrixId: String): ContactsDisplayBeanV2? {
        return dao.getDelContactByMatrixId(matrixId)
    }

    fun getContactByContactId(contactId: String): ContactsDisplayBean? {
        return dao.getContactByContactId(contactId)?.toContactsDisplayBean()
    }

    fun getContactByContactIdLive(contactId: String): LiveData<ContactsDisplayBean?>? {
        val contactsDisplayBean =
            dao.getContactByContactIdLive(contactId).value?.toContactsDisplayBean()
        return MutableLiveData<ContactsDisplayBean?>(contactsDisplayBean)
    }

    fun getContactByMatrixIdLive(matrixId: String): LiveData<ContactsDisplayBean?> {
        val contactsDisplayBean =
            dao.getContactByMatrixIdLive(matrixId).value?.toContactsDisplayBean()
        return MutableLiveData<ContactsDisplayBean?>(contactsDisplayBean)
    }

    fun getContactsByMatrixIds(matrixIds: List<String>): List<ContactsDisplayBean> {
        return contactV2ListToContactList(dao.getContactsByMatrixIds(matrixIds))
    }

    fun searchContacts(keyword: String): List<ContactsDisplayBean> {
        return contactV2ListToContactList(dao.searchContacts(keyword))
    }

    fun updateLastSeen(matrixId: String, lastSeenTs: Long): Int {
        return dao.updateLastSeen(matrixId, lastSeenTs)
    }

    fun getRecentContacts(isAsc: Boolean = false): List<ContactsDisplayBean> {
        return contactV2ListToContactList(dao.getRecentContacts(isAsc))
    }

    private fun contactV2ListToContactList(contactV2s: List<ContactsDisplayBeanV2>): List<ContactsDisplayBean> {
        val contacts = mutableListOf<ContactsDisplayBean>()
        contactV2s.forEach {
            contacts.add(it.toContactsDisplayBean())
        }
        return contacts
    }

    private fun contactListToContactV2List(contacts: List<ContactsDisplayBean>): List<ContactsDisplayBeanV2> {
        val contactV2s = mutableListOf<ContactsDisplayBeanV2>()
        contacts.forEach {
            contactV2s.add(it.toContactsDisplayBeanV2())
        }
        return contactV2s
    }

    companion object {
        private var INSTANCE: ContactDaoHelper? = null

        fun getInstance() = INSTANCE ?: ContactDaoHelper(
            AppDatabase.getInstance(BaseModule.getContext()).contactDaoV2()
        ).also { INSTANCE = it }
    }
}
