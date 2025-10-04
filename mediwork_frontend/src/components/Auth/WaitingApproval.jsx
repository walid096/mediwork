import React from "react";
import { useAuth } from "../../contexts/AuthContext";

const WaitingApproval = ({ onMenuClick }) => {
      const { user, logout } = useAuth();
  
      const handleLogout = () => {
          logout();
      };
  
  return (
    <div
      style={{
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        minHeight: "60vh",
        textAlign: "center",
        padding: "2rem",
      }}
    >
      {/* Hourglass icon */}
      <div style={{ marginTop: "2rem", marginBottom: "1.5rem" }}>
        <span role="img" aria-label="hourglass" style={{ fontSize: "3rem" }}>
          ⏳
        </span>
      </div>

    
      <h2>Votre compte est en attente d'approbation</h2>
      <p>
        Merci de vous être inscrit. Votre compte doit être approuvé par un
        administrateur avant que vous puissiez accéder à l'application.
        <br />
        Vous recevrez un email dès que votre compte sera activé.
      </p>
  {/* Logout button below hourglass, styled red */}
      <button
        type="button"
        onClick={handleLogout}
        className="px-5 py-2 mb-4 mt-10 bg-red-600 text-white font-semibold rounded-lg shadow-md hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-400 focus:ring-opacity-75 transition-colors duration-300"
        style={{ minWidth: '120px' }}
      >
        Logout
      </button>    </div>
  );
};

export default WaitingApproval;
