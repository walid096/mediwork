import { yupResolver } from "@hookform/resolvers/yup";
import React, { useState } from "react";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../contexts/AuthContext";
import { userValidationSchema } from "../../utils/validation";

const RegisterForm = () => {
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { register: registerUser, login } = useAuth();
  const navigate = useNavigate();

  const {
    register,
    handleSubmit,
    formState: { errors, isValid },
  } = useForm({
    resolver: yupResolver(userValidationSchema),
    mode: "onBlur",
  });

  const onSubmit = async (data) => {
    setIsSubmitting(true);
    setError("");

    console.log("Form Data:", data); // debug

    const result = await registerUser(data);

    if (result.success) {
      // Try to log in the user automatically after registration
      const loginResult = await login({
        email: data.email,
        password: data.password,
      });
      if (loginResult.success) {
        navigate("/waiting-approval");
      } else {
        // If login fails, fallback to waiting approval page
        navigate("/register");
      }
    } else {
      setError(result.error);
    }

    setIsSubmitting(false);
  };

  return (
    <div className="min-h-screen flex items-center justify-center dark:bg-gray-900 bg-white">
      <div className="flex w-full max-w-sm mx-auto overflow-hidden shadow-xl bg-white rounded-lg shadow-lg dark:bg-gray-800 lg:max-w-4xl">
        <div className="hidden bg-cover lg:block lg:w-1/2 bg-[url('https://images.unsplash.com/photo-1524758631624-e2822e304c36?q=80&w=2070&auto=format&fit=crop')]"></div>

        <div className="w-full px-6 py-8 md:px-8 lg:w-1/2">
          <div className="flex justify-center mx-auto">
            <img
              className="w-auto h-7 sm:h-8 dark:invert"
              src="https://www.sqli.com/themes/custom/sqli_theme/Logo_midnight_500x343.png"
              alt="SQLI Logo"
            />
          </div>

          <p className="mt-3 text-xl text-center text-gray-600 dark:text-gray-200">
            Create an account
          </p>

          <form className="mt-4" onSubmit={handleSubmit(onSubmit)}>

            {/* First Name and Last Name on the same line */}
            <div className="flex gap-4">
              {/* First Name */}
              <div className="flex-1">
                <label className="block mb-2 text-sm font-medium text-gray-600 dark:text-gray-200">
                  First Name
                </label>
                <input
                  type="text"
                  {...register("firstName")}
                  disabled={isSubmitting}
                  className="w-full px-4 py-2 mt-2 text-gray-700 bg-white border rounded-md dark:bg-gray-900 dark:text-white dark:border-gray-600 focus:border-blue-500 focus:outline-none focus:ring"
                />
                {errors.firstName && (
                  <p className="mt-1 text-xs text-red-500">{errors.firstName.message}</p>
                )}
              </div>

              {/* Last Name */}
              <div className="flex-1">
                <label className="block mb-2 text-sm font-medium text-gray-600 dark:text-gray-200">
                  Last Name
                </label>
                <input
                  type="text"
                  {...register("lastName")}
                  disabled={isSubmitting}
                  className="w-full px-4 py-2 mt-2 text-gray-700 bg-white border rounded-md dark:bg-gray-900 dark:text-white dark:border-gray-600 focus:border-blue-500 focus:outline-none focus:ring"
                />
                {errors.lastName && (
                  <p className="mt-1 text-xs text-red-500">{errors.lastName.message}</p>
                )}
              </div>
            </div>

            {/* Email */}
            <label className="block mt-4 mb-2 text-sm font-medium text-gray-600 dark:text-gray-200">
              Email
            </label>
            <input
              type="email"
              {...register("email")}
              autoComplete="username"
              disabled={isSubmitting}
              className="w-full px-4 py-2 mt-2 text-gray-700 bg-white border rounded-md dark:bg-gray-900 dark:text-white dark:border-gray-600 focus:border-blue-500 focus:outline-none focus:ring"
            />
            {errors.email && (
              <p className="mt-1 text-xs text-red-500">{errors.email.message}</p>
            )}

            {/* Password */}
            <label className="block mt-4 mb-2 text-sm font-medium text-gray-600 dark:text-gray-200">
              Password
            </label>
            <input
              type="password"
              {...register("password")}
              autoComplete="new-password"
              disabled={isSubmitting}
              className="w-full px-4 py-2 mt-2 text-gray-700 bg-white border rounded-md dark:bg-gray-900 dark:text-white dark:border-gray-600 focus:border-blue-500 focus:outline-none focus:ring"
            />
            {errors.password && (
              <p className="mt-1 text-xs text-red-500">{errors.password.message}</p>
            )}

            {/* Matricule */}
            <label className="block mt-4 mb-2 text-sm font-medium text-gray-600 dark:text-gray-200">
              Matricule
            </label>
            <input
              type="text"
              {...register("matricule")}
              disabled={isSubmitting}
              className="w-full px-4 py-2 mt-2 text-gray-700 bg-white border rounded-md dark:bg-gray-900 dark:text-white dark:border-gray-600 focus:border-blue-500 focus:outline-none focus:ring"
            />
            {errors.matricule && (
              <p className="mt-1 text-xs text-red-500">{errors.matricule.message}</p>
            )}

            {/* Role */}

            <select {...register("role")} defaultValue="PENDING" hidden>
              <option value="PENDING">Pending</option>
            </select>
            {errors.role && (
              <p className="mt-1 text-xs text-red-500">{errors.role.message}</p>
            )}

            {/* Error message */}
            {error && (
              <div className="mt-4 text-sm text-center text-red-600 dark:text-red-400">
                {error}
              </div>
            )}

            {/* Submit Button */}
            <div className="mt-6">
              <button
                type="submit"
                disabled={isSubmitting || !isValid}
                className="w-full px-6 py-3 text-sm font-medium tracking-wide text-white capitalize transition-colors duration-300 transform bg-gray-800 rounded-lg hover:bg-gray-700 focus:outline-none focus:ring focus:ring-gray-300 focus:ring-opacity-50 disabled:opacity-50"
              >
                {isSubmitting ? "Creating Account..." : "Sign Up"}
              </button>
            </div>
          </form>

          {/* Sign In Redirect */}
          <div className="flex items-center justify-between mt-4">
            <span className="w-1/5 border-b dark:border-gray-600 md:w-1/4"></span>

            <a
              href="/"
              className="text-xs text-gray-500 uppercase dark:text-gray-400 hover:underline"
            >
              Already have an account? Sign In
            </a>

            <span className="w-1/5 border-b dark:border-gray-600 md:w-1/4"></span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RegisterForm;
