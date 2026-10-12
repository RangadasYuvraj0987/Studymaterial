// Admin login token, kept in the browser so a page refresh does not log the admin out.
const KEY = "adminToken";

export const getToken = () => {
  try {
    return localStorage.getItem(KEY);
  } catch {
    return null;
  }
};

export const setToken = (token) => {
  try {
    localStorage.setItem(KEY, token);
  } catch {
    /* storage blocked: admin must log in again after refresh */
  }
};

export const clearToken = () => {
  try {
    localStorage.removeItem(KEY);
  } catch {
    /* ignore */
  }
};
