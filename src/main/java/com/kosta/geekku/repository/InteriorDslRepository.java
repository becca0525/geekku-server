package com.kosta.geekku.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.kosta.geekku.dto.SampleDto;
import com.kosta.geekku.entity.Interior;
import com.kosta.geekku.entity.InteriorRequest;
import com.kosta.geekku.entity.InteriorSample;
import com.kosta.geekku.entity.QInterior;
import com.kosta.geekku.entity.QInteriorBookmark;
import com.kosta.geekku.entity.QInteriorRequest;
import com.kosta.geekku.entity.QInteriorSample;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

@Repository
public class InteriorDslRepository {

	@Autowired
	private JPAQueryFactory jpaQueryFactory;

	public List<Interior> findInteriorListForMain() throws Exception {
		QInterior interior = QInterior.interior;
		return jpaQueryFactory.selectFrom(interior).orderBy(interior.createdAt.desc()).limit(9).fetch();
	}

	public List<InteriorSample> findSampleListForMain() throws Exception {
		QInteriorSample sample = QInteriorSample.interiorSample;
		return jpaQueryFactory.selectFrom(sample).orderBy(sample.createdAt.desc()).limit(9).fetch();
	}

	public Long interiorCountAll() throws Exception {
		QInterior interior = QInterior.interior;
		return jpaQueryFactory.select(interior.count()).from(interior).fetchOne();
	}

	public Long interiorCountByLoc(String possibleLocation) throws Exception {
		QInterior interior = QInterior.interior;
		return jpaQueryFactory.select(interior.count()).from(interior)
				.where(interior.possibleLocation.eq(possibleLocation)).fetchOne();
	}

	public List<Interior> interiorListAll() throws Exception {
		QInterior interior = QInterior.interior;
		return jpaQueryFactory.selectFrom(interior).orderBy(interior.createdAt.desc()).fetch();
	}

	public List<Interior> interiorListByLoc(String possibleLocation) throws Exception {
		QInterior interior = QInterior.interior;
		return jpaQueryFactory.selectFrom(interior).where(interior.possibleLocation.eq(possibleLocation))
				.orderBy(interior.createdAt.desc()).fetch();
	}

	public Integer findInteriorBookmark(UUID userId, Integer interiorNum) throws Exception {
		QInteriorBookmark interiorBookmark = QInteriorBookmark.interiorBookmark;

		return jpaQueryFactory.select(interiorBookmark.bookmarkInteriorNum).from(interiorBookmark)
				.where(interiorBookmark.userId.eq(userId).and(interiorBookmark.bookmarkInteriorNum.eq(interiorNum)))
				.fetchOne();
	}

	public List<InteriorSample> sampleListByFilter(String date, String type, String style, Integer size,
			String location) {
		QInteriorSample sample = QInteriorSample.interiorSample;
		BooleanBuilder filter = new BooleanBuilder();
//		if (date != null) {
//			filter.and(sample.createdAt.eq(date));
//		}
		if (type != null) {
			filter.and(sample.type.eq(type));
		}
		if (style != null) {
			filter.and(sample.style.eq(style));
		}
		if (size != null) {
			filter.and(sample.size.eq(size));
		}
		if (location != null) {
			filter.and(sample.location.eq(location));
		}
		
		JPAQuery<InteriorSample> query = jpaQueryFactory.selectFrom(sample).where(filter);
		if (date != null) {
			if ("latest".equalsIgnoreCase(date)) {
				query.orderBy(sample.createdAt.desc());
			} else if ("oldest".equalsIgnoreCase(date)) {
				query.orderBy(sample.createdAt.asc());
			}
		}
		return query.fetch();
	}
	public Long sampleCountByFilter(String date, String type, String style, Integer size,
			String location) throws Exception {
		QInteriorSample sample = QInteriorSample.interiorSample;
		BooleanBuilder filter = new BooleanBuilder();
		
		if (type != null) {
			filter.and(sample.type.eq(type));
		}
		if (style != null) {
			filter.and(sample.style.eq(style));
		}
		if (size != null) {
			filter.and(sample.size.eq(size));
		}
		if (location != null) {
			filter.and(sample.location.eq(location));
		}
		
		return jpaQueryFactory.select(sample.count()).from(sample)
				.where(filter)
				.fetchOne();				
	}
	
	public List<InteriorSample> interiorSampleListmypage(PageRequest pageRequest, UUID companyId) throws Exception {
		QInteriorSample interiorSample = QInteriorSample.interiorSample;
		return jpaQueryFactory.selectFrom(interiorSample).where(interiorSample.company.companyId.eq(companyId))
				.orderBy(interiorSample.createdAt.desc()).fetch();
	}

	/*
	 * public List<InteriorSample> interiorReviewListmypage(PageRequest pageRequest,
	 * UUID companyId) throws Exception { QInteriorRequest interiorRequest =
	 * QInteriorSample.interiorSample; return
	 * jpaQueryFactory.selectFrom(interiorSample).where(interiorSample.company.
	 * companyId.eq(companyId)) .orderBy(interiorSample.createdAt.desc()).fetch(); }
	 */

	/*
	 * public List<InteriorRequest> interiorRequestList(PageRequest pageRequest,
	 * UUID companyId) throws Exception { QInteriorRequest interiorRequest =
	 * QInteriorRequest.interiorRequest; return
	 * jpaQueryFactory.selectFrom(interiorRequest).where(interiorRequest.company.
	 * companyId.eq(companyId)) .orderBy(interiorRequest.createdAt.desc()).fetch();
	 * }
	 */

	public Long findMypageEstateCount(UUID companyId) throws Exception {
		QInteriorSample interiorsample = QInteriorSample.interiorSample;

		return jpaQueryFactory.select(interiorsample.count()).from(interiorsample)
				.where(interiorsample.company.companyId.eq(companyId)).fetchOne();
	}

}
