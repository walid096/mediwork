import * as yup from 'yup';

export const loginValidationSchema = yup.object({
    email: yup.string()
        .email("Please enter a valid email address")
        .required("Email is required"),

    password: yup.string()
        .min(8, "Password must be at least 8 characters")
        .max(50, "Password cannot be longer than 50 characters")
        .matches(/^(?=.*[A-Z])/, "Password must contain at least 1 uppercase letter")
        .matches(/^(?=.*[a-z])/, "Password must contain at least 1 lowercase letter")
        .matches(/^(?=.*\d)/, "Password must contain at least 1 digit")
        .matches(/^(?=.*[\W_])/, "Password must contain at least 1 special character")
        .required("Password is required")
});

export const userValidationSchema = yup.object({
    firstName: yup.string().required("First name is required"),
    lastName: yup.string().required("Last name is required"),
    email: yup.string().email("Please enter a valid email address").required("Email is required"),
    password: yup.string()
        .min(8, "Password must be at least 8 characters")
        .max(50, "Password cannot be longer than 50 characters")
        .matches(/^(?=.*[A-Z])/, "Password must contain at least 1 uppercase letter")
        .matches(/^(?=.*[a-z])/, "Password must contain at least 1 lowercase letter")
        .matches(/^(?=.*\d)/, "Password must contain at least 1 digit")
        .matches(/^(?=.*[\W_])/, "Password must contain at least 1 special character")
        .required("Password is required"),
    role: yup.string().required("Role is required"),
    matricule: yup.string().required("Matricule is required")
});