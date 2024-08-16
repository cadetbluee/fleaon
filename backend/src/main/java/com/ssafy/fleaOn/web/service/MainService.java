package com.ssafy.fleaOn.web.service;

import com.ssafy.fleaOn.web.domain.*;
import com.ssafy.fleaOn.web.dto.*;
import com.ssafy.fleaOn.web.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MainService {

    private final LiveRepository liveRepository;

    private final ShortsRepository shortsRepository;

    private final CategoryRepository categoryRepository;

    private final ProductRepository productRepository;

    private final RegionInfoRepository regionInfoRepository;

    private final UserRegionRepository userRegionRepository;

    private final TradeRepository tradeRepository;
    private final LiveScrapRepository liveScrapRepository;
    private final ShortsScrapRepository shortsScrapRepository;

    public Slice<MainLiveResponse> getMainLiveListByRegionCode(int userId, List<UserRegion> findUserRegionList, int page) {
        // 모든 데이터를 수집할 리스트
        List<MainLiveResponse> allLiveResponses = new ArrayList<>();
        List<Live> resultLiveList = new ArrayList<>();

        // 모든 지역 코드를 탐색하여 데이터를 수집
        for (UserRegion userRegion : findUserRegionList) {
            String regionCode = userRegion.getRegion().getRegionCode();
            // 해당 지역 코드의 모든 Live 데이터를 가져오고, isLive가 0 또는 1인 항목만 추가
            List<Live> findLiveList = liveRepository.findByRegionInfo_regionCode(regionCode);
            resultLiveList.addAll(findLiveList.stream()
                    .filter(live -> live.getIsLive() == 0 || live.getIsLive() == 1)
                    .collect(Collectors.toList()));
        }

        // isLive가 1인 것들을 먼저, 그리고 isLive가 0인 것들을 나중에 정렬하고, 각 그룹 내에서 liveDate를 오름차순으로 정렬
        resultLiveList.sort(Comparator.comparing(Live::getIsLive).reversed()
                .thenComparing(Live::getLiveDate));

        // 각 Live 객체에 대해 MainLiveResponse 객체를 생성하여 allLiveResponses 리스트에 추가
        for (Live live : resultLiveList) {
            Optional<List<Product>> findProductList = productRepository.findByLive_LiveId(live.getLiveId());
            Optional<LiveScrap> findLiveScrap = liveScrapRepository.findByUser_userIdAndLive_liveId(userId, live.getLiveId());
            if (findProductList.isPresent()) {
                boolean isScrap = findLiveScrap.isPresent();
                MainLiveResponse mainLiveResponse = MainLiveResponse.fromEntity(live, findProductList.get(), isScrap);
                allLiveResponses.add(mainLiveResponse);
            }
        }

        // 페이지네이션 설정
        Pageable pageable = PageRequest.of(page, 10);

        // 전체 데이터를 페이지네이션 처리하여 특정 페이지의 데이터를 추출
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allLiveResponses.size());
        List<MainLiveResponse> content = allLiveResponses.subList(start, end);

        // 다음 페이지가 있는지 여부를 판단
        boolean hasNext = end < allLiveResponses.size();

        return new SliceImpl<>(content, pageable, hasNext);
    }



    public List<UserRegion> getUserRegionByUserId(int userId) {
        Optional<List<UserRegion>> userRegionList = userRegionRepository.findByUser_userId(userId);
        return Optional.ofNullable(userRegionList.get()).orElse(Collections.emptyList());
    }

    public Slice<MainShortsResponse> getMainShortsListByUploadDate(int userId, int page) {
        Pageable pageable = PageRequest.of(page, 10);
        List<MainShortsResponse> mainShortsResponseList = new ArrayList<>();

        // Shorts 데이터 가져오기 (Slice로)
        Slice<Shorts> shortsSlice = shortsRepository.findAllByOrderByUploadDateDesc(pageable);

        for (Shorts shorts : shortsSlice) {
            Optional<Product> product = productRepository.findByProductId(shorts.getProduct().getProductId());
            Optional<Live> live = liveRepository.findByLiveId(product.get().getLive().getLiveId());
            boolean isScrap = shortsScrapRepository.findByUser_userIdAndShorts_shortsId(userId, shorts.getShortsId()).isPresent();

            if (product.isPresent() && live.isPresent()) {
                MainShortsResponse mainShortsResponse = MainShortsResponse.fromEntity(shorts, product.get(), live.get(), isScrap);
                mainShortsResponseList.add(mainShortsResponse);
            }
        }

        // hasNext를 shortsSlice에서 직접 가져옴
        boolean hasNext = shortsSlice.hasNext();

        return new SliceImpl<>(mainShortsResponseList, pageable, hasNext);
    }


    public Optional<List<Category>> getMainCategoryList() {
        return Optional.of(categoryRepository.findAll());
    }

    public Slice<Map<String, Object>> getSearchResultByName(String name, int userId) {
        Pageable pageable = PageRequest.of(0, 10);

        // 1. 이름에 따른 상품 검색 (상품명에 키워드가 포함된 경우)
        Slice<Product> findProductSlice = productRepository.findByNameIgnoreCaseContaining(name, pageable);

        // 2. 카테고리 이름에 따른 상품 검색
        Slice<Product> categoryProductSlice = productRepository.findByCategoryName(name, pageable);

        // 3. 검색된 모든 상품을 합침
        Set<Product> allProducts = new HashSet<>();
        allProducts.addAll(findProductSlice.getContent());
        allProducts.addAll(categoryProductSlice.getContent());

        // 4. 각 상품에 대해 연관된 라이브와 쇼츠를 검색하여 결과를 구성
        List<ResultUpcomingResponse> upcomingResponseList = new ArrayList<>();
        List<ResultLiveResponse> liveResponseList = new ArrayList<>();
        List<ResultShortsResponse> shortsResponseList = new ArrayList<>();

        for (Product product : allProducts) {
            Live live = liveRepository.findByLiveId(product.getLive().getLiveId()).orElse(null);
            Shorts shorts = shortsRepository.findByProduct_ProductId(product.getProductId()).orElse(null);

            // 관련 라이브와 쇼츠만 필터링하여 추가
            if (live != null) {
                if (live.getIsLive() == 0) {
                    ResultUpcomingResponse upcomingResponse = ResultUpcomingResponse.fromEntity(live, product);
                    upcomingResponseList.add(upcomingResponse);
                } else if (live.getIsLive() == 1 || live.getIsLive() == 2) {
                    ResultLiveResponse liveResponse = ResultLiveResponse.fromEntity(live, product);
                    liveResponseList.add(liveResponse);
                }
            }

            if (shorts != null) {
                ResultShortsResponse shortsResponse = ResultShortsResponse.fromEntity(shorts, product, live);
                shortsResponseList.add(shortsResponse);
            }
        }

        // 5. 결과를 맵에 담아 반환
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("upcoming", upcomingResponseList);
        resultMap.put("live", liveResponseList);
        resultMap.put("shorts", shortsResponseList);

        List<Map<String, Object>> resultList = new ArrayList<>();
        resultList.add(resultMap);

        // 6. Slice로 반환 (페이지네이션 고려)
        boolean hasNext = findProductSlice.hasNext() || categoryProductSlice.hasNext();
        return new SliceImpl<>(resultList, pageable, hasNext);
    }


    public List<SidoNameResponse> getSidoNameList() {
        return regionInfoRepository.findDistinctSido();
    }

    public List<GugunNameResponse> getGugunNameBySidoName(String sidoName) {
        return regionInfoRepository.findDistinctGugunBySido(sidoName);
    }

    public List<EupmyeonNameResponse> getEupmyeonNameAndRegionCodeBySidoNameAndGugunName(String sidoName, String gugunName) {
        return regionInfoRepository.findDistinctBySidoAndGugun(sidoName, gugunName);
    }
}
