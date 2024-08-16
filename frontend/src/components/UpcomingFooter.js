import React from "react";
import { Box, Typography } from "@mui/material";
import LocationOnIcon from "@mui/icons-material/LocationOn";

const UpcomingFooter = ({ name, dongName, title, price }) => {
  return (
    <Box
      sx={{
        top: 206,
        left: "7.50px",
        width: "100%",
        textAlign: "left",
      }}
    >
      <Typography
        sx={{
          color: "white",
          fontSize: 8,
          fontWeight: 400,
        }}
      >
        <LocationOnIcon sx={{ fontSize: "8px" }} />
        {dongName}
      </Typography>

      <Typography
        sx={{
          color: "white",
          fontSize: 14,
          fontWeight: 600,
          overflow: "hidden",
          textOverflow: "ellipsis",
          whiteSpace: "nowrap",
        }}
      >
        {title}
      </Typography>
    </Box>
  );
};

export default UpcomingFooter;
