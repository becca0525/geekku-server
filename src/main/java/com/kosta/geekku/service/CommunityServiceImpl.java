package com.kosta.geekku.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.kosta.geekku.dto.CommunityDto;
import com.kosta.geekku.dto.CommunityFilterDto;
import com.kosta.geekku.entity.Community;
import com.kosta.geekku.entity.CommunityBookmark;
import com.kosta.geekku.entity.CommunityComment;
import com.kosta.geekku.entity.User;
import com.kosta.geekku.repository.CommunityBookmarkRepository;
import com.kosta.geekku.repository.CommunityCommentRepository;
import com.kosta.geekku.repository.CommunityRepository;
import com.kosta.geekku.repository.UserRepository;
import com.kosta.geekku.util.CommunitySpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {

	private final CommunityRepository communityRepository;
	private final CommunityBookmarkRepository communityBookmarkRepository;
	private final UserRepository userRepository;
	private final CommunityCommentRepository communityCommentRepository;

	@Value("${upload.path}")
	private String uploadPath;

	@Override
	public Page<CommunityDto> getCommunityList(Pageable pageable) {
		return communityRepository.findAll(pageable).map(Community::toDto);
	}

	@Override
	public Integer createCommunity(CommunityDto communityDto) {
		Community community = communityDto.toEntity();
		communityRepository.save(community);
		return community.getCommunityNum();
	}

	@Override
	public CommunityDto getCommunityDetail(Integer communityNum) {
		return communityRepository.findById(communityNum)
				.orElseThrow(() -> new IllegalArgumentException("커뮤니티 조회에 실패했습니다.")).toDto();
	}

	@Override
	public Page<CommunityDto> getFilteredCommunityList(CommunityFilterDto filterDto, Pageable pageable) {
		Specification<Community> specification = CommunitySpecification.filterBy(filterDto);
		return communityRepository.findAll(specification, pageable).map(Community::toDto);
	}

	@Transactional
	@Override
	public void createCommunityWithCoverImage(String title, String content, String type, MultipartFile coverImage, String userId)
	        throws Exception {
	    // userId를 UUID로 변환 (userId가 UUID 형식이라고 가정)
	    UUID userUUID;
	    try {
	        userUUID = UUID.fromString(userId);  // String을 UUID로 변환
	    } catch (IllegalArgumentException e) {
	        throw new IllegalArgumentException("유효하지 않은 userId 형식입니다.");
	    }

	    // userRepository에서 userId(UUID)로 사용자 조회
	    User user = userRepository.findByUserId(userUUID);  // UUID로 조회
	    if (user == null) {
	        throw new IllegalArgumentException("유효하지 않은 사용자입니다.");
	    }

	    // 커뮤니티 객체 생성
	    Community community = Community.builder()
	            .title(title)
	            .content(content)
	            .type(type)
	            .user(user)  // userId 추가
	            .build();
	    community = communityRepository.save(community);
	    System.out.println("Community saved with ID: " + community.getCommunityNum());

	    // 파일 저장 처리
	    if (coverImage == null || coverImage.isEmpty()) {
	        throw new IllegalArgumentException("파일이 비어 있거나 업로드되지 않았습니다.");
	    }

	    try {
	        // 파일 저장 경로 확인
	        File uploadDir = new File(uploadPath);
	        if (!uploadDir.exists()) {
	            uploadDir.mkdirs();
	        }

	        // 파일 이름과 경로 설정
	        String fileName = coverImage.getOriginalFilename();
	        if (fileName == null) {
	            throw new IllegalArgumentException("파일 이름이 없습니다.");
	        }
	        String filePath = uploadPath + "/" + fileName;
	        File dest = new File(filePath);  // 파일 객체 생성

	        // 파일을 지정한 경로로 전송
	        coverImage.transferTo(dest);
	        community.setCoverImage(filePath);  // 파일 경로를 엔티티에 업데이트
	        communityRepository.save(community);  // 업데이트된 커뮤니티 저장

	        System.out.println("파일 경로가 데이터베이스에 저장되었습니다: " + fileName);
	    } catch (IOException e) {
	        e.printStackTrace();
	        System.err.println("파일 저장 중 오류 발생: " + e.getMessage());
	        throw new RuntimeException("파일 저장에 실패했습니다. 경로를 확인하세요.");
	    }
	}

	@Override
	public void updateCommunity(Integer id, CommunityDto communityDto, MultipartFile coverImage) throws Exception {
		// 기존 데이터 조회
		Community community = communityRepository.findById(id)
				.orElseThrow(() -> new Exception("해당 커뮤니티 글을 찾을 수 없습니다."));

		// 데이터 업데이트
		community.setTitle(communityDto.getTitle());
		community.setContent(communityDto.getContent());
		community.setType(communityDto.getType());

		// 파일 처리 (새로운 파일이 있을 경우만)
		if (coverImage != null && !coverImage.isEmpty()) {
			// 기존 파일 삭제
			if (community.getCoverImage() != null) {
				File existingFile = new File(uploadPath, community.getCoverImage());
				if (existingFile.exists()) {
					existingFile.delete();
				}
			}
			// 새로운 파일 저장
			String fileName = UUID.randomUUID() + "_" + coverImage.getOriginalFilename();
			String filePath = uploadPath + "/" + fileName;
			coverImage.transferTo(new File(filePath));
			// 파일 이름 업데이트
			community.setCoverImage(fileName);
		}

		// 변경 내용 저장
		communityRepository.save(community);
	}

	@Override
	@Transactional
	public boolean toggleCommunityBookmark(String userId, Integer communityNum) throws Exception {
		CommunityBookmark existingBookmark = communityBookmarkRepository
				.findByUserUserIdAndCommunityCommunityNum(UUID.fromString(userId), communityNum);
		if (existingBookmark == null) {
		//	User user = userRepository.findById(UUID.fromString(userId))
		//			.orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

			User user = userRepository.findByUserId(UUID.fromString(userId));
//					.orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다.")); <- 수정함

			Community community = communityRepository.findByCommunityNum(communityNum)
					.orElseThrow(() -> new IllegalArgumentException("해당 커뮤니티를 찾을 수 없습니다."));
			communityBookmarkRepository.save(CommunityBookmark.builder().user(user).community(community).build());
			return true; // 북마크 활성화
		} else {
			communityBookmarkRepository.delete(existingBookmark);
			return false; // 북마크 비활성화
		}
	}

	@Transactional
	@Override
	public void createComment(Integer communityId, String userId, String content) throws Exception {
		// 커뮤니티 게시글 확인
		Community community = communityRepository.findById(communityId)
				.orElseThrow(() -> new Exception("해당 커뮤니티 글을 찾을 수 없습니다."));
		// 사용자 확인
		User user = userRepository.findById(UUID.fromString(userId))
				.orElseThrow(() -> new Exception("해당 사용자를 찾을 수 없습니다."));
		// 댓글 엔티티 생성 및 저장
		CommunityComment comment = CommunityComment.builder().community(community).user(user).content(content).build();

		communityCommentRepository.save(comment);
	}

	@Transactional
	@Override
	public void deleteComment(Integer commentId) throws Exception {
		// 댓글 존재 여부 확인
		CommunityComment comment = communityCommentRepository.findById(commentId)
				.orElseThrow(() -> new Exception("해당 댓글을 찾을 수 없습니다."));
		// 댓글 삭제
		communityCommentRepository.delete(comment);
	}

	@Override
	public User getUserProfile(String userId) throws Exception {
		return userRepository.findById(UUID.fromString(userId)).orElseThrow(() -> new Exception("해당 유저를 찾을 수 없습니다."));
	}

	@Override
	public List<Community> getUserCommunities(String userId) throws Exception {
		List<Community> communities = communityRepository.findAllByUser_UserId(UUID.fromString(userId));
		return communities.stream().map(community -> Community.builder().title(community.getTitle())
				.viewCount(community.getViewCount()).build()).collect(Collectors.toList());
	}


	@Override
	public List<CommunityDto> getCommunityListForMain() throws Exception {
		Page<Community> page = communityRepository.findAll(PageRequest.of(0, 3, Sort.by(Sort.Order.desc("viewCount"))));
        return page.getContent().stream().map(c -> c.toDto()).collect(Collectors.toList());
	}
}
