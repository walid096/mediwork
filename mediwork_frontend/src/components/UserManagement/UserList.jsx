import React, { useEffect, useState } from "react";
import PageWrapper from "../Layout/PageWrapper";

export default function UserList({ title, role, fetchUsers, createUser, updateUser, deleteUser, labels }) {
  const [users, setUsers] = useState([]);
  const [page, setPage] = useState(0);
  const [rowsPerPage] = useState(10);
  const [loading, setLoading] = useState(false);

  const [showAddModal, setShowAddModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);

  const [selectedUser, setSelectedUser] = useState(null);
  const [formData, setFormData] = useState(initialForm(role));

  function initialForm(roleValue) {
    return {
      firstName: "",
      lastName: "",
      email: "",
      matricule: "",
      password: "",
      role: roleValue
    };
  }

  const loadUsers = async () => {
    setLoading(true);
    try {
      const result = await fetchUsers();
      setUsers(result);
    } catch (error) {
      alert(`Erreur lors du chargement: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadUsers();
  }, []);

  const handleAddUser = async () => {
    try {
      setLoading(true);
      await createUser(formData);
      setShowAddModal(false);
      setFormData(initialForm(role));
      loadUsers();
      alert(labels.addSuccess);
    } catch (error) {
      alert(`Erreur lors de l'ajout: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  const handleEditUser = async () => {
    try {
      setLoading(true);
      await updateUser(selectedUser.id, formData);
      setShowEditModal(false);
      setFormData(initialForm(role));
      loadUsers();
      alert(labels.editSuccess);
    } catch (error) {
      alert(`Erreur lors de la modification: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteUser = async (id) => {
    if (!window.confirm(labels.deleteConfirm)) return;
    try {
      setLoading(true);
      await deleteUser(id);
      loadUsers();
      alert(labels.deleteSuccess);
    } catch (error) {
      alert(`Erreur lors de la suppression: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  const openEditModal = (user) => {
    setSelectedUser(user);
    setFormData({ ...user });
    setShowEditModal(true);
  };

  return (
    <PageWrapper title={title}>
      <div className="mb-4">
        <button onClick={() => setShowAddModal(true)} className="bg-blue-500 text-white px-4 py-2 rounded">
          {labels.addButton}
        </button>
      </div>

      {loading ? (
        <p>Chargement...</p>
      ) : (
        <table className="min-w-full border">
          <thead>
            <tr>
              <th className="border px-2">Prénom</th>
              <th className="border px-2">Nom</th>
              <th className="border px-2">Email</th>
              <th className="border px-2">Matricule</th>
              <th className="border px-2">Actions</th>
            </tr>
          </thead>
          <tbody>
            {users.slice(page * rowsPerPage, (page + 1) * rowsPerPage).map((user) => (
              <tr key={user.id}>
                <td className="border px-2">{user.firstName}</td>
                <td className="border px-2">{user.lastName}</td>
                <td className="border px-2">{user.email}</td>
                <td className="border px-2">{user.matricule}</td>
                <td className="border px-2">
                  <button onClick={() => openEditModal(user)} className="bg-yellow-400 text-white px-2 py-1 rounded mr-2">
                    Modifier
                  </button>
                  <button onClick={() => handleDeleteUser(user.id)} className="bg-red-500 text-white px-2 py-1 rounded">
                    Supprimer
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {/* Modal Ajout */}
      {(showAddModal || showEditModal) && (
        <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-40">
          <div className="bg-white p-6 rounded shadow-lg w-96">
            <h2 className="mb-4">{showAddModal ? labels.addTitle : labels.editTitle}</h2>
            {renderForm(formData, setFormData)}
            <div className="flex justify-end mt-4">
              <button onClick={() => { showAddModal ? setShowAddModal(false) : setShowEditModal(false); }} className="mr-2 px-3 py-1 border rounded">
                Annuler
              </button>
              <button onClick={showAddModal ? handleAddUser : handleEditUser} className={`px-3 py-1 rounded ${showAddModal ? 'bg-blue-500' : 'bg-green-500'} text-white`}>
                {showAddModal ? 'Ajouter' : 'Modifier'}
              </button>
            </div>
          </div>
        </div>
      )}
    </PageWrapper>
  );
}

function renderForm(formData, setFormData) {
  return (
    <>
      <input type="text" placeholder="Prénom" value={formData.firstName} onChange={(e) => setFormData({ ...formData, firstName: e.target.value })} className="border p-2 mb-2 w-full" />
      <input type="text" placeholder="Nom" value={formData.lastName} onChange={(e) => setFormData({ ...formData, lastName: e.target.value })} className="border p-2 mb-2 w-full" />
      <input type="email" placeholder="Email" value={formData.email} onChange={(e) => setFormData({ ...formData, email: e.target.value })} className="border p-2 mb-2 w-full" />
      <input type="text" placeholder="Matricule" value={formData.matricule} onChange={(e) => setFormData({ ...formData, matricule: e.target.value })} className="border p-2 mb-2 w-full" />
      <input type="password" placeholder="Mot de passe" value={formData.password} onChange={(e) => setFormData({ ...formData, password: e.target.value })} className="border p-2 mb-2 w-full" />
    </>
  );
}
