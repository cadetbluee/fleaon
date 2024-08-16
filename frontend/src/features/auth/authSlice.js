// src/features/auth/authSlice.js
import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import baseAxios from "../../utils/httpCommons";

// Helper functions to handle localStorage
const loadState = () => {
  try {
    const serializedState = localStorage.getItem("authState");
    if (serializedState === null) {
      return undefined;
    }
    return JSON.parse(serializedState);
  } catch (err) {
    return undefined;
  }
};

const saveState = (state) => {
  try {
    const serializedState = JSON.stringify(state);
    localStorage.setItem("authState", serializedState);
  } catch (err) {
    // Ignore write errors
  }
};

const initialState = loadState() || {
  isAuthenticated: false,
  user: {
    userId: null,
    email: null,
    userIdentifier: null,
    profilePicture: null,
    name: null,
    nickname: null,
    role: null,
    phone: null,
    level: null,
  },
  token: null,
};

export const updateUserInfo = createAsyncThunk(
  "auth/updateUserInfo",
  async ({ data }, { rejectWithValue }) => {
    try {
      const response = await baseAxios().post(`/fleaon/users/extraInfo`, data);
      console.log("response : ", response);
      return response.data;
    } catch (error) {
      console.error("Error in updateUserInfo:", error);
      if (error.response && error.response.data) {
        // 서버가 반환한 오류 메시지 처리
        return rejectWithValue(error.response.data);
      } else {
        // 기타 오류 처리
        return rejectWithValue(error.message);
      }
    }
  }
);
const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {
    login(state, action) {
      state.isAuthenticated = true;
      state.token = action.payload.token;
      saveState(state);
    },
    logout(state) {
      state.isAuthenticated = false;
      state.user = null;
      state.token = null;
      saveState(state);
    },
    setUser(state, action) {
      state.user = action.payload.user || null;
      saveState(state);
    },
    setPhone(state, action) {
      state.user.phone = action.payload;
      saveState(state);
    },
    setNickname(state, action) {
      state.user.nickname = action.payload;
      saveState(state);
    },
    setUserRegion(state, action) {
      state.user.dongName = action.payload.dongName;
      state.user.regionCode = action.payload.regionCode;
      saveState(state);
    },
    updateUserLevel(state, action) {
      if (state.user) {
        state.user.level = action.payload;
        saveState(state);
      }
    },
  },
  extraReducers: (builder) => {
    builder.addCase(updateUserInfo.fulfilled, (state, action) => {
      state.user = { ...state.user, ...action.payload };
      saveState(state);
    });
  },
});

export const {
  login,
  logout,
  setUser,
  setPhone,
  setNickname,
  setUserRegion,
  updateUserLevel,
} = authSlice.actions;
export default authSlice.reducer;
