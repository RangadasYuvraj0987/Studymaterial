import axios from "axios";
import { getToken, clearToken } from "../auth";

const api = axios.create({ baseURL: import.meta.env.VITE_API_URL });
export default api;

// Send the admin token (only admins have one) with every request
api.interceptors.request.use((config) => {
  const token = getToken();
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// If the backend says the admin token is no longer valid, go back to the login page
api.interceptors.response.use(
  (res) => res,
  (err) => {
    const url = err.config?.url || "";
    if (err.response?.status === 401 && getToken() && url !== "/admin/login") {
      clearToken();
      window.location.href = "/admin/login";
    }
    return Promise.reject(err);
  }
);

// Turns any API / network error into a short readable message
export function errorMessage(err) {
  const d = err.response?.data;
  if (!err.response) {
    return err.message === "Network Error" ? "Cannot reach the server" : err.message;
  }
  if (d?.message) return d.message;
  if (d && typeof d === "object") return Object.values(d).join(", ");
  return "Something went wrong";
}
