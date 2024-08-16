import React, { useEffect } from "react";
import { useSelector, useDispatch } from "react-redux";
import { useNavigate } from "react-router-dom";
import { fetchSellLive, fetchSellShorts } from "../actions.js";
import Switch from "../../../components/Switch";
import Live from "./SellLive.js";
import Shorts from "./SellShorts.js";
import { Box, Typography, Grid } from "@mui/material";
import ArrowBackIosNewIcon from "@mui/icons-material/ArrowBackIosNew";
import { switchTab } from "../sellSlice.js";

const SellList = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const selectedTab = useSelector((state) => state.sell.selectedTab);
  const live = useSelector((state) => state.sell.live.data);
  const shorts = useSelector((state) => state.sell.shorts.data);
  const email = useSelector((state) => state.auth.user.email);

  useEffect(() => {
    dispatch(fetchSellLive(email));
    dispatch(fetchSellShorts(email));
  }, [dispatch, email]);

  const switchOptions = [
    { value: "live", label: "LIVE" },
    { value: "shorts", label: "SHORTS" },
  ];
  const handleBackButtonClick = () => {
    // 이전 페이지로 이동
    navigate("/mypage");
    // 상태 초기화
    dispatch(switchTab("live"));
  };

  return (
    <Box sx={{ mb: 18 }}>
      <Box
        sx={{
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          width: "100%",
          position: "fixed",
          bgcolor: "white",
          pt: 2,
          pb: 3.5,
          left: 0,
          zIndex: 1000,
        }}
      >
        <Box
          onClick={handleBackButtonClick}
          sx={{
            cursor: "pointer",
            position: "absolute",
            left: 24,
          }}
        >
          <ArrowBackIosNewIcon />
        </Box>
        <Typography
          sx={{
            fontSize: "24px",
            fontWeight: "600",
            textAlign: "center",
          }}
        >
          판매내역
        </Typography>
      </Box>
      <Box container sx={{ paddingTop: 10 }}>
        {selectedTab === "live" && <Live items={live} />}
        {selectedTab === "shorts" && <Shorts items={shorts} />}
        <Grid
          sx={{
            position: "fixed",
            bottom: 100,
            left: "50%",
            transform: "translateX(-50%)",
          }}
        >
          <Switch options={switchOptions} type="sell" />
        </Grid>
      </Box>
    </Box>
  );
};

export default SellList;
