import React from "react";
import { useSelector, useDispatch } from "react-redux";
import { toggleScrap } from "../features/mypage/scrapSlice";
import {
  Box,
  Typography,
  Modal,
  List,
  ListItem,
  IconButton,
} from "@mui/material";
import BookmarkBorderIcon from "@mui/icons-material/BookmarkBorder";
import BookmarkIcon from "@mui/icons-material/Bookmark";
import styles from "../styles/UpcomingModal.module.css";
import { formatDateTime, formatPrice } from "../utils/cssUtils";
import { useNavigate } from "react-router-dom";

const UpcomingModal = ({
  id,
  open,
  handleClose,
  liveDate,
  productNames = [],
  productPrices = [],
  title,
  thumbnail,
  author,
  tradePlace,
  scrap,
}) => {
  const dispatch = useDispatch();

  const handleScrapToggle = () => {
    dispatch(toggleScrap({ id }));
  };

  const handleEditLive = () => {
    // navigate(`/edit-live/${liveDetail.liveId}`, { state: { liveDetail } });
  };

  return (
    <Modal
      open={open}
      onClose={handleClose}
      BackdropProps={{
        style: {
          backgroundColor: "transparent",
        },
      }}
    >
      <div className={styles.modalOverlay}>
        <Box className={styles.modalContent}>
          <div className={styles.modalHeader}>
            <img
              src={liveDetail.liveThumbnail}
              alt="thumbnail"
              className={styles.thumbnailImage}
            />
            <div className={styles.textContainer}>
              <Typography variant="h6" component="h2" className={styles.title}>
                <span className={styles.titleText}>{title}</span>
                <IconButton onClick={handleScrapToggle}>
                  {scrap ? (
                    <BookmarkIcon className={styles.bookmarkIcon} />
                  ) : (
                    <BookmarkBorderIcon className={styles.bookmarkIcon} />
                  )}
                </IconButton>
              </Typography>
              <Typography
                variant="body2"
                component="p"
                className={styles.author}
              >
                {liveDetail?.user.nickname}
              </Typography>
              <Typography
                variant="body2"
                component="p"
                className={styles.liveDate}
              >
                방송 시간 {formatDateTime(liveDetail.liveDate)}
              </Typography>
            </div>
            <span className={styles.closeButton} onClick={handleClose}>
              &times;
            </span>
          </div>
          <div className={styles.modalHeader2}>
            <div className={styles.textContainer}>
              <Typography
                variant="body2"
                component="p"
                className={styles.tradePlace}
              >
                <div className={styles.tradeplacetext}>거래 장소</div>
                <div>{liveDetail.tradePlace}</div>
              </Typography>
            </div>
          </div>
          <div className={styles.modalBody}>
            <div className={styles.listtitle}>판매 상품 목록</div>
            <List className={styles.productList}>
              {liveDetail.products.map((product, index) => (
                <ListItem key={index} className={styles.productItem}>
                  <div className={styles.productName}>{product.name}</div>
                  <div className={styles.productPrice}>
                    {formatPrice(product.price)}
                  </div>
                </ListItem>
              ))}
            </List>
          </div>

          {auth && auth === user && (
            <Box
              sx={{
                width: "100%",
                justifyContent: "center",
                alignItems: "center",
                display: "flex",
                mt: 10,
              }}
            >
              <Button
                variant="contained"
                onClick={handleEditLive}
                sx={{
                  width: "95%",
                  backgroundColor: "#FF0B55",
                  color: "white",
                  fontSize: 16,
                  borderRadius: 2,
                }}
              >
                라이브 수정하기
              </Button>
            </Box>
          )}
        </Box>
      </div>
    </Modal>
  );
};

export default UpcomingModal;
