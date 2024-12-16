package com.mallangs.domain.board.controller;

import com.mallangs.domain.board.dto.request.*;
import com.mallangs.domain.board.dto.response.*;
import com.mallangs.domain.board.entity.BoardStatus;
import com.mallangs.domain.board.entity.BoardType;
import com.mallangs.domain.board.service.BoardService;
import com.mallangs.domain.member.entity.embadded.UserId;
import com.mallangs.global.exception.ErrorCode;
import com.mallangs.global.exception.MallangsCustomException;
import com.mallangs.global.jwt.entity.CustomMemberDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Tag(name = "커뮤니티 & 실종신고-목격제보 API", description = "커뮤니티/실종신고-목격제보 관련 API")
@RestController
@RequestMapping("/api/v1/board")
@RequiredArgsConstructor
@Slf4j
public class BoardController {

    private final BoardService boardService;

    // 커뮤니티 관련
    @Operation(summary = "커뮤니티 게시글 작성", description = "커뮤니티 게시판에 글을 작성합니다.")
    @PostMapping("/community")
    public ResponseEntity<Long> createCommunity(@Valid @RequestBody CommunityCreateRequest request,
                                                @AuthenticationPrincipal CustomMemberDetails customMemberDetails) {
        if (customMemberDetails == null) throw new MallangsCustomException(ErrorCode.LOGIN_REQUIRED);
        Long boardId = boardService.createCommunityBoard(request, customMemberDetails.getMemberId());
        return ResponseEntity.created(URI.create("/api/board/community/" + boardId)).body(boardId);
    }

    @Operation(summary = "커뮤니티 게시글 전체 조회", description = "커뮤니티 게시글을 모두 조회합니다.")
    @GetMapping("/community")
    public ResponseEntity<PageResponse<CommunityListResponse>> getAllCommunity(@RequestParam(defaultValue = "1") int page) {
        PageRequest pageRequest = PageRequest.of(page - 1, 10);
        return ResponseEntity.ok(PageResponse.from(boardService.getAllCommunityBoard(pageRequest)));
    }

    @Operation(summary = "커뮤니티 게시글 키워드 조회", description = "커뮤니티 게시글을 키워드로 조회합니다.")
    @GetMapping("/community/keyword")
    public ResponseEntity<PageResponse<CommunityListResponse>> searchCommunityPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page
    ) {
        PageRequest pageRequest = PageRequest.of(page - 1, 10);
        return ResponseEntity.ok(PageResponse.from(boardService.searchCommunityBoards(keyword, pageRequest)));
    }

    @Operation(summary = "커뮤니티 게시글 상세 조회(By 회원 ID)", description = "커뮤니티 게시글을 회원 ID로 조회합니다.")
    @GetMapping("/community/member/{stringUserId}")
    public ResponseEntity<PageResponse<CommunityListResponse>> getMemberCommunityPosts(
            @PathVariable String stringUserId,
            @RequestParam(defaultValue = "1") int page
    ) {
        UserId userId = new UserId(stringUserId);
        PageRequest pageRequest = PageRequest.of(page - 1, 10);
        return ResponseEntity.ok(PageResponse.from(boardService.getMemberCommunityBoards(userId, pageRequest)));
    }

    @Operation(summary = "커뮤니티 게시글 상세 조회(By 게시글 번호)", description = "커뮤니티 게시글을 게시글 번호로 조회합니다.")
    @GetMapping("/community/{boardId}")
    public ResponseEntity<CommunityDetailResponse> getCommunityPost(
            @Parameter(description = "게시글 ID") @PathVariable Long boardId
    ) {
        return ResponseEntity.ok(boardService.getCommunityBoard(boardId));
    }

    @Operation(summary = "커뮤니티 게시글 상세 조회(By 카테고리 이름)", description = "커뮤니티 게시글을 카테고리 이름으로 검색합니다.")
    @GetMapping("/community/category/{categoryName}")
    public ResponseEntity<PageResponse<CommunityListResponse>> getCommunityPostByCategory(
            @PathVariable String categoryName,
            @RequestParam(defaultValue = "1") int page
    ) {
        log.info("=== Request received - category: {}, page: {}", categoryName, page);

        PageRequest pageRequest = PageRequest.of(page - 1, 10);

        var result = boardService.getCommunityBoardsByCategory(categoryName, pageRequest);
        log.info("=== Request processed successfully");

        return ResponseEntity.ok(PageResponse.from(result));
//        return ResponseEntity.ok(PageResponse.from(boardService.getCommunityBoardsByCategory(categoryName, pageRequest)));
    }

    @Operation(summary = "커뮤니티 특정 게시글 수정", description = "특정 커뮤니티 게시글을 수정합니다.")
    @PutMapping("/community/{boardId}")
    public ResponseEntity<Void> updateCommunityPost(
            @PathVariable Long boardId,
            @Valid @RequestBody CommunityUpdateRequest request,
            @AuthenticationPrincipal CustomMemberDetails customMemberDetails
    ) {
        if (customMemberDetails == null) throw new MallangsCustomException(ErrorCode.LOGIN_REQUIRED);
        boardService.updateCommunityBoard(boardId, request, customMemberDetails.getMemberId());
        return ResponseEntity.noContent().build();
    }

    // 실종신고-목격제보 관련
    @Operation(summary = "실종신고-목격제보 게시글 작성", description = "새로운 실종신고-목격제보 게시글을 작성합니다.")
    @PostMapping("/sighting")
    public ResponseEntity<Long> createSightingPost(
            @Valid @RequestBody SightingCreateRequest request,
            @AuthenticationPrincipal CustomMemberDetails customMemberDetails
    ) {
        if (customMemberDetails == null) throw new MallangsCustomException(ErrorCode.LOGIN_REQUIRED);
        Long boardId = boardService.createSightingBoard(request, customMemberDetails.getMemberId());
        return ResponseEntity.created(URI.create("/api/board/sighting/" + boardId)).body(boardId);
    }

    @Operation(summary = "실종신고-목격제보 게시글 전체 조회", description = "실종신고-목격제보 게시글을 모두 조회합니다.")
    @GetMapping("/sighting")
    public ResponseEntity<PageResponse<SightingListResponse>> getAllSighting(@RequestParam(defaultValue = "1") int page) {
        PageRequest pageRequest = PageRequest.of(page - 1, 10);
        return ResponseEntity.ok(PageResponse.from(boardService.getAllSightingBoard(pageRequest)));
    }

    @Operation(summary = "실종신고-목격제보 게시글 키워드 조회", description = "실종신고-목격제보 게시글을 키워드로 조회합니다.")
    @GetMapping("/sighting/keyword")
    public ResponseEntity<PageResponse<SightingListResponse>> searchSightingPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page
    ) {
        PageRequest pageRequest = PageRequest.of(page - 1, 10);
        return ResponseEntity.ok(PageResponse.from(boardService.searchSightingBoards(keyword, pageRequest)));
    }

    @Operation(summary = "실종신고-목격제보 게시글 상세 조회(By 회원 ID)", description = "실종신고-목격제보 게시글을 회원 ID로 조회합니다.")
    @GetMapping("/sighting/member/{stringUserId}")
    public ResponseEntity<PageResponse<SightingListResponse>> getMemberSightingPosts(
            @PathVariable String stringUserId,
            @RequestParam(defaultValue = "1") int page
    ) {
        UserId userId = new UserId(stringUserId);
        PageRequest pageRequest = PageRequest.of(page - 1, 10);
        return ResponseEntity.ok(PageResponse.from(boardService.getMemberSightingBoards(userId, pageRequest)));
    }

    @Operation(summary = "실종신고-목격제보 게시글 상세 조회(By 게시글 번호)", description = "실종신고-목격제보 게시글을 게시글 번호로 조회합니다.")
    @GetMapping("/sighting/{boardId}")
    public ResponseEntity<SightingDetailResponse> getSightingPost(
            @Parameter(description = "게시글 ID")@PathVariable Long boardId) {
        return ResponseEntity.ok(boardService.getSightingBoard(boardId));
    }

    @Operation(summary = "실종신고-목격제보 게시글 상세 조회(By 카테고리 이름)", description = "실종신고-목격제보 게시글을 카테고리 이름으로 검색합니다.")
    @GetMapping("/sighting/category/{categoryName}")
    public ResponseEntity<PageResponse<SightingListResponse>> getSightingPostsByCategory(
            @PathVariable String categoryName,
            @RequestParam(defaultValue = "1") int page
    ) {
        PageRequest pageRequest = PageRequest.of(page - 1, 10);
        return ResponseEntity.ok(PageResponse.from(boardService.getSightingBoardsByCategory(categoryName, pageRequest)));
    }

    @Operation(summary = "실종신고-목격제보 게시글 수정", description = "기존 실종신고-목격제보 게시글을 수정합니다.")
    @PutMapping("/sighting/{boardId}")
    public ResponseEntity<Void> updateSightingPost(
            @PathVariable Long boardId,
            @Valid @RequestBody SightingUpdateRequest request,
            @AuthenticationPrincipal CustomMemberDetails customMemberDetails
    ) {
        if (customMemberDetails == null) throw new MallangsCustomException(ErrorCode.LOGIN_REQUIRED);
        boardService.updateSightingBoard(boardId, request, customMemberDetails.getMemberId());
        return ResponseEntity.noContent().build();
    }

    // 게시물 삭제(공통)
    @Operation(summary = "게시글 삭제", description = "커뮤니티 또는 실종신고-목격제보 게시물의 상태를 HIDDEN으로 변경합니다.")
    @DeleteMapping("/{boardId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long boardId,
            @RequestParam BoardType boardType,
            @AuthenticationPrincipal CustomMemberDetails customMemberDetails
    ) {
        boardService.deleteBoard(boardId, customMemberDetails.getMemberId(), boardType);
        return ResponseEntity.noContent().build();
    }

    // 관리자 기능
    @Operation(summary = "게시글 검색 - 관리자 권한", description = "관리자가 게시글을 검색하고 필터링합니다.")
    @GetMapping("/admin/search")
    public ResponseEntity<AdminBoardsResponse> searchPosts(
            @RequestParam(required = false) BoardType boardType,
            @RequestParam(required = false) BoardStatus boardStatus,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page
    ) {
        PageRequest pageRequest = PageRequest.of(page - 1, 10);

        if (boardStatus != null && categoryId != null) {
            return ResponseEntity.ok(boardService.searchBoardsForAdminWithStatus(categoryId, boardType, boardStatus, keyword, pageRequest));
        } else if (categoryId != null) {
            return ResponseEntity.ok(boardService.searchBoardsForAdmin(categoryId, boardType, keyword, pageRequest));
        }
        return ResponseEntity.ok(boardService.getBoardsByStatus(boardStatus, pageRequest));
    }

    @Operation(summary = "게시글 상태 변경 - 관리자 권한", description = "관리자가 게시글의 상태를 변경합니다. 다중 선택 가능")
    @PatchMapping("/admin/status")
    public ResponseEntity<Void> changePostsStatus(@RequestBody @Valid AdminBoardStatusRequest request) {
        boardService.changeBoardStatus(request.getBoardIds(), request.getStatus());
        return ResponseEntity.noContent().build();
    }
}
