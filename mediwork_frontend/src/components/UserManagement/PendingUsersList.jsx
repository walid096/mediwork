import React, { useEffect, useState } from "react";
import { ROLES } from "../../config/roles";
import api from "../../config/api";
import { useAuth } from "../../contexts/AuthContext";
import { RotateCcw } from "lucide-react";

const PendingUsersList = () => {
  const [pendingUsers, setPendingUsers] = useState([]);
  const [selectedRoles, setSelectedRoles] = useState({});
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  
  const { refreshAccessToken } = useAuth();

  const fetchPendingUsers = async () => {
    setLoading(true);
    setError(null);
    
    try {
      const res = await api.get("/admin/users");
      const data = Array.isArray(res.data) ? res.data : [];
      const pending = data.filter(user => user.role === ROLES.PENDING);
      setPendingUsers(pending);
      setError(null);
    } catch (error) {
      // ✅ CLEANED: Remove console.error for production
      // console.error("API Error:", error.response?.status, error.response?.statusText);
      
      if (error.response?.status === 403) {
        try {
          // ✅ CLEANED: Remove console.log for production
          // console.log("🔄 Token expired, attempting refresh...");
          await refreshAccessToken();
          
          const retryRes = await api.get("/admin/users");
          const retryData = Array.isArray(retryRes.data) ? retryRes.data : [];
          const retryPending = retryData.filter(user => user.role === ROLES.PENDING);
          setPendingUsers(retryPending);
          setError(null);
        } catch (refreshError) {
          // ✅ CLEANED: Remove console.error for production
          // console.error("❌ Token refresh failed:", refreshError);
          setError("Session expirée. Veuillez vous reconnecter.");
          setPendingUsers([]);
        }
      } else {
        setError("Erreur lors du chargement des utilisateurs");
        setPendingUsers([]);
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPendingUsers();
    
    const interval = setInterval(fetchPendingUsers, 30000);
    return () => clearInterval(interval);
  }, []);

  const handleSelectChange = (userId, role) => {
    setSelectedRoles(prev => ({ ...prev, [userId]: role }));
  };

  const handleApprove = async (userId) => {
    const role = selectedRoles[userId];
    
    if (!role) {
      alert("Veuillez sélectionner un rôle avant d'approuver.");
      return;
    }

    try {
      await api.put(`/admin/users/${userId}/role`, role, {
          headers: { 'Content-Type': 'application/json' }
      });
      
      await fetchPendingUsers();
      setSelectedRoles(prev => {
          const newSelected = { ...prev };
          delete newSelected[userId];
          return newSelected;
      });
      alert("Rôle attribué avec succès!");
    } catch (err) {
      if (err.response?.status === 403) {
        try {
          await refreshAccessToken();
          
          await api.put(`/admin/users/${userId}/role`, role, {
              headers: { 'Content-Type': 'application/json' }
          });
          await fetchPendingUsers();
          setSelectedRoles(prev => {
              const newSelected = { ...prev };
              delete newSelected[userId];
              return newSelected;
          });
          alert("Rôle attribué avec succès!");
        } catch (retryError) {
          alert("Erreur lors de l'assignation du rôle: " + (retryError.response?.data?.message || retryError.message));
        }
      } else {
        alert("Erreur lors de l'assignation du rôle: " + (err.response?.data?.message || err.message));
      }
    }
  };

  return (
    <div className="mx-auto py-4 rounded-xl">
      <div className="flex justify-between items-center mb-4">
        <h3 className="text-lg font-semibold text-gray-800">Utilisateurs en attente d'approbation</h3>
        <button
          onClick={fetchPendingUsers}
          disabled={loading}
          className="bg-gray-200 hover:bg-gray-300 text-gray-800 font-semibold px-4 py-2 rounded-lg shadow disabled:opacity-50"
        >
            
          {loading ? '...' : ''}
          <RotateCcw />
        </button>
      </div>
      
      {error && (
        <div className="mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded">
          {error}
        </div>
      )}
      
      <div className="space-y-2">
        {pendingUsers.length === 0 ? (
          <div className="text-center text-gray-500 py-4">
            {loading ? 'Chargement des utilisateurs...' : 'Aucun utilisateur en attente d\'approbation'}
          </div>
        ) : (
          pendingUsers.map(user => (
            <div
              key={user.id}
              className="flex flex-col md:flex-row items-center justify-between bg-white rounded-lg px-6 py-2"
            >
              <div className="flex-1 min-w-0">
                <div className="text-lg font-semibold">{user.firstName} {user.lastName}</div>
                <div className="text-base">{user.email}</div>
              </div>
              <div className="flex items-center space-x-4 mt-4 md:mt-0">
                <select
                  value={selectedRoles[user.id] || ""}
                  onChange={(e) => handleSelectChange(user.id, e.target.value)}
                  className="bg-gray-200 rounded-lg px-4 py-2 font-semibold"
                >
                  <option value="" disabled>Attribuer un rôle</option>
                  <option value={ROLES.RH}>RH</option>
                  <option value={ROLES.DOCTOR}>Médecin</option>
                  <option value={ROLES.COLLABORATOR}>Collaborateur</option>
                </select>
                <button
                  onClick={() => handleApprove(user.id)}
                  className="bg-blue-700 text-white font-bold px-5 py-2 rounded-lg shadow hover:from-blue-700 hover:to-purple-700 transition-colors"
                >
                  Approuver
                </button>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default PendingUsersList;