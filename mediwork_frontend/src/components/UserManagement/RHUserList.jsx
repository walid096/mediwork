import React, { useEffect, useState } from "react";
import { userApi } from '../../api/userApi';
import { ROLES } from '../../config/roles';
import PageWrapper from '../Layout/PageWrapper';

export default function RHUserList() {
  const [users, setUsers] = useState([]);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [loading, setLoading] = useState(false);
  const [showAddModal, setShowAddModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    matricule: '',
    password: '',
    role: 'RH'
  });

  // Charger les RH depuis l'API - UPDATED to use role-specific endpoint
  const loadRHUsers = async () => {
    setLoading(true);
    try {
      // Use the new role-specific endpoint instead of filtering
      const rhUsers = await userApi.getRHUsers();
      setUsers(rhUsers);
    } catch (error) {
      console.error('Erreur lors du chargement des RH:', error);
      // Show user-friendly error message
      alert(`Erreur lors du chargement: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadRHUsers();
  }, []);

  // CRUD Operations - UPDATED with proper error handling
  const handleAddUser = async () => {
    try {
      setLoading(true);
      await userApi.createUser(formData);
      setShowAddModal(false);
      setFormData({ firstName: '', lastName: '', email: '', matricule: '', password: '', role: 'RH' });
      loadRHUsers(); // Refresh the list
      alert('Utilisateur RH ajouté avec succès!');
    } catch (error) {
      console.error('Erreur lors de l\'ajout:', error);
      alert(`Erreur lors de l'ajout: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  const handleEditUser = async () => {
    try {
      setLoading(true);
      await userApi.updateUser(selectedUser.id, formData);
      setShowEditModal(false);
      setSelectedUser(null);
      setFormData({ firstName: '', lastName: '', email: '', matricule: '', password: '', role: 'RH' });
      loadRHUsers(); // Refresh the list
      alert('Utilisateur modifié avec succès!');
    } catch (error) {
      console.error('Erreur lors de la modification:', error);
      alert(`Erreur lors de la modification: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  const handleArchiveUser = async (user) => {
    if (window.confirm(`Êtes-vous sûr de vouloir archiver ${user.firstName} ${user.lastName} ?`)) {
      try {
        setLoading(true);
        await userApi.archiveUser(user.id);
        loadRHUsers(); // Refresh the list
        alert('Utilisateur archivé avec succès!');
      } catch (error) {
        console.error('Erreur lors de l\'archivage:', error);
        alert(`Erreur lors de l'archivage: ${error.message}`);
      } finally {
        setLoading(false);
      }
    }
  };

  const handleRestoreUser = async (user) => {
    try {
      setLoading(true);
      await userApi.restoreUser(user.id);
      loadRHUsers(); // Refresh the list
      alert('Utilisateur restauré avec succès!');
    } catch (error) {
      console.error('Erreur lors de la restauration:', error);
      alert(`Erreur lors de la restauration: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  // Form handlers
  const handleInputChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const openAddModal = () => {
    setFormData({ firstName: '', lastName: '', email: '', matricule: '', password: '', role: 'RH' });
    setShowAddModal(true);
  };

  const openEditModal = (user) => {
    setSelectedUser(user);
    setFormData({
      firstName: user.firstName,
      lastName: user.lastName,
      email: user.email,
      matricule: user.matricule,
      password: '', // Password is empty for edit (not shown)
      role: user.role
    });
    setShowEditModal(true);
  };

  // Pagination
  const totalUsers = users.length;
  const totalPages = Math.ceil(totalUsers / rowsPerPage);
  const paginatedUsers = users.slice(page * rowsPerPage, (page + 1) * rowsPerPage);

  const renderPageNumbers = () => {
    const pages = [];
    for (let i = 0; i < totalPages; i++) {
      pages.push(
        <button
          key={i}
          onClick={() => setPage(i)}
          className={`px-3 py-1 text-sm rounded border ${
            i === page
              ? "bg-blue-600 text-white border-blue-600"
              : "text-gray-700 border-gray-300 hover:bg-gray-100"
          }`}
        >
          {i + 1}
        </button>
      );
    }
    return pages;
  };

  return (
    <PageWrapper title="Ressources Humaines" className="p-6 bg-white min-h-screen">
        <section className="mx-auto w-full max-w-7xl px-4 py-4">
        {/* En-tête */}
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-lg font-semibold">Liste des utilisateurs RH</h2>
          <div className="flex space-x-2">
            <button
              onClick={loadRHUsers}
              disabled={loading}
              className="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
            >
              {loading ? 'Chargement...' : 'Rafraîchir'}
            </button>
            <button
              onClick={openAddModal}
              disabled={loading}
              className="px-4 py-2 text-sm font-medium text-white bg-green-600 rounded hover:bg-green-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
            >
              Ajouter
            </button>
          </div>
        </div>

        {/* Tableau */}
        <div className="overflow-x-auto border border-gray-300 rounded-xl shadow-lg bg-gradient-to-br from-white to-gray-50">
          <table className="min-w-full text-sm">
            <thead className="sticky top-0 z-10 bg-blue-50">
              <tr>
                <th className="px-6 py-3 font-semibold text-blue-900 text-left border-b border-blue-200 rounded-tl-xl">Infos RH</th>
                <th className="px-6 py-3 font-semibold text-blue-900 text-left border-b border-blue-200">Email</th>
                <th className="px-6 py-3 font-semibold text-blue-900 text-left border-b border-blue-200">Rôle</th>
                <th className="px-6 py-3 font-semibold text-blue-900 text-left border-b border-blue-200">Statut</th>
                <th className="px-6 py-3 font-semibold text-blue-900 text-left border-b border-blue-200 rounded-tr-xl">Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="5" className="px-6 py-6 text-center text-gray-500">Chargement...</td>
                </tr>
              ) : paginatedUsers.length > 0 ? (
                paginatedUsers.map((user, idx) => (
                  <tr key={user.id} className={"transition-colors duration-150 " + (idx % 2 === 0 ? "bg-white" : "bg-blue-50") + " hover:bg-blue-100"}>
                    <td className="px-6 py-4 font-medium text-gray-900 border-b border-gray-200">
                      <div className="mb-1"><span className="font-bold text-blue-700">ID:</span> {user.id}</div>
                      <div className="mb-1"><span className="font-bold text-blue-700">Matricule:</span> {user.matricule}</div>
                      <div className="mb-1"><span className="font-bold text-blue-700">Prénom:</span> {user.firstName}</div>
                      <div><span className="font-bold text-blue-700">Nom:</span> {user.lastName}</div>
                    </td>
                    <td className="px-6 py-4 text-gray-700 border-b border-gray-200">{user.email}</td>
                    <td className="px-6 py-4 text-gray-700 border-b border-gray-200">{user.role}</td>
                    <td className="px-6 py-4 text-gray-700 border-b border-gray-200">
                      <span className={`px-2 py-1 text-xs rounded-full ${
                        user.archived ? 'bg-red-100 text-red-800' : 'bg-green-100 text-green-800'
                      }`}>
                        {user.archived ? 'Archivé' : 'Actif'}
                      </span>
                    </td>
                    <td className="px-6 py-4 border-b border-gray-200">
                      <div className="flex space-x-2">
                        <button
                          onClick={() => openEditModal(user)}
                          title="Modifier"
                          disabled={loading}
                          className="inline-flex items-center justify-center text-blue-600 hover:bg-blue-100 hover:text-blue-900 p-2 rounded-full transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                          <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15.232 5.232l3.536 3.536M9 13l6.536-6.536a2 2 0 112.828 2.828L11.828 15.828a2 2 0 01-2.828 0L9 13zm-6 6v-3a2 2 0 012-2h3" />
                          </svg>
                        </button>
                        {!user.archived ? (
                          <button
                            onClick={() => handleArchiveUser(user)}
                            title="Archiver"
                            disabled={loading}
                            className="inline-flex items-center justify-center text-orange-600 hover:bg-orange-100 hover:text-orange-900 p-2 rounded-full transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                          >
                            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 8h14M5 8a2 2 0 110-4h14a2 2 0 110 4M5 8v10a2 2 0 002 2h10a2 2 0 002-2V8m-14 0h14" />
                            </svg>
                          </button>
                        ) : (
                          <button
                            onClick={() => handleRestoreUser(user)}
                            title="Restaurer"
                            disabled={loading}
                            className="inline-flex items-center justify-center text-green-600 hover:bg-green-100 hover:text-green-900 p-2 rounded-full transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                          >
                            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                            </svg>
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="5" className="px-6 py-6 text-center text-gray-500">
                    Aucun utilisateur RH trouvé.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination améliorée */}
        <div className="flex justify-center items-center space-x-2 mt-4">
          <button
            onClick={() => setPage(Math.max(0, page - 1))}
            disabled={page === 0}
            className={`px-3 py-1 text-sm rounded border ${
              page === 0
                ? "text-gray-400 border-gray-200 cursor-not-allowed"
                : "text-gray-700 border-gray-300 hover:bg-gray-100"
            }`}
          >
            Précédent
          </button>

          {renderPageNumbers()}

          <button
            onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
            disabled={page >= totalPages - 1}
            className={`px-3 py-1 text-sm rounded border ${
              page >= totalPages - 1
                ? "text-gray-400 border-gray-200 cursor-not-allowed"
                : "text-gray-700 border-gray-300 hover:bg-gray-100"
            }`}
          >
            Suivant
          </button>
        </div>

        {/* Add User Modal */}
        {showAddModal && (
          <div className="fixed inset-0 bg-blue-50 bg-opacity-40 flex items-center justify-center z-50">
            <div className="bg-white rounded-2xl shadow-md shadow-gray-200 p-8 w-full max-w-md border border-gray-100">
              <h3 className="text-2xl font-medium text-gray-800 mb-6 text-center">Ajouter un utilisateur RH</h3>
              <div className="space-y-5">
                <input
                  type="text"
                  name="firstName"
                  placeholder="Prénom"
                  value={formData.firstName}
                  onChange={handleInputChange}
                  className="w-full px-4 py-3 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-400 focus:border-blue-400 outline-none text-gray-800 bg-gray-50 transition"
                  required
                />
                <input
                  type="text"
                  name="lastName"
                  placeholder="Nom"
                  value={formData.lastName}
                  onChange={handleInputChange}
                  className="w-full px-4 py-3 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-400 focus:border-blue-400 outline-none text-gray-800 bg-gray-50 transition"
                  required
                />
                <input
                  type="email"
                  name="email"
                  placeholder="Email"
                  value={formData.email}
                  onChange={handleInputChange}
                  className="w-full px-4 py-3 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-400 focus:border-blue-400 outline-none text-gray-800 bg-gray-50 transition"
                  required
                />
                <input
                  type="text"
                  name="matricule"
                  placeholder="Matricule"
                  value={formData.matricule}
                  onChange={handleInputChange}
                  className="w-full px-4 py-3 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-400 focus:border-blue-400 outline-none text-gray-800 bg-gray-50 transition"
                  required
                />
                <input
                  type="password"
                  name="password"
                  placeholder="Mot de passe"
                  value={formData.password}
                  onChange={handleInputChange}
                  className="w-full px-4 py-3 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-400 focus:border-blue-400 outline-none text-gray-800 bg-gray-50 transition"
                  required
                />
              </div>
              <div className="flex justify-end space-x-3 mt-8">
                <button
                  onClick={() => setShowAddModal(false)}
                  className="px-5 py-2 text-gray-600 border border-gray-300 rounded-lg hover:bg-gray-100 transition"
                >
                  Annuler
                </button>
                <button
                  onClick={handleAddUser}
                  disabled={loading}
                  className="px-5 py-2 bg-blue-600 text-white rounded-lg font-medium hover:bg-blue-700 transition disabled:bg-gray-400 disabled:cursor-not-allowed"
                >
                  {loading ? 'Ajout...' : 'Ajouter'}
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Edit User Modal */}
        {showEditModal && (
          <div className="fixed inset-0 bg-blue-50 bg-opacity-40 flex items-center justify-center z-50">
            <div className="bg-white rounded-2xl shadow-md shadow-gray-200 p-8 w-full max-w-md border border-gray-100">
              <h3 className="text-2xl font-medium text-gray-800 mb-6 text-center">Modifier l'utilisateur</h3>
              <div className="space-y-5">
                <input
                  type="text"
                  name="firstName"
                  placeholder="Prénom"
                  value={formData.firstName}
                  onChange={handleInputChange}
                  className="w-full px-4 py-3 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-400 focus:border-blue-400 outline-none text-gray-800 bg-gray-50 transition"
                  required
                />
                <input
                  type="text"
                  name="lastName"
                  placeholder="Nom"
                  value={formData.lastName}
                  onChange={handleInputChange}
                  className="w-full px-4 py-3 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-400 focus:border-blue-400 outline-none text-gray-800 bg-gray-50 transition"
                  required
                />
                <input
                  type="email"
                  name="email"
                  placeholder="Email"
                  value={formData.email}
                  onChange={handleInputChange}
                  className="w-full px-4 py-3 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-400 focus:border-blue-400 outline-none text-gray-800 bg-gray-50 transition"
                  required
                />
                <input
                  type="text"
                  name="matricule"
                  placeholder="Matricule"
                  value={formData.matricule}
                  onChange={handleInputChange}
                  className="w-full px-4 py-3 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-400 focus:border-blue-400 outline-none text-gray-800 bg-gray-50 transition"
                  required
                />
              </div>
              <div className="flex justify-end space-x-3 mt-8">
                <button
                  onClick={() => setShowEditModal(false)}
                  className="px-5 py-2 text-gray-600 border border-gray-300 rounded-lg hover:bg-gray-100 transition"
                >
                  Annuler
                </button>
                <button
                  onClick={handleEditUser}
                  disabled={loading}
                  className="px-5 py-2 bg-blue-600 text-white rounded-lg font-medium hover:bg-blue-700 transition disabled:bg-gray-400 disabled:cursor-not-allowed"
                >
                  {loading ? 'Modification...' : 'Modifier'}
                </button>
              </div>
            </div>
          </div>
        )}
      </section>
    </PageWrapper>
  );
}