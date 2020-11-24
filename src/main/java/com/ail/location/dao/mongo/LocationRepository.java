//package com.ail.location.dao.mongo;
//
//import com.ail.location.model.mongo.Location;
//import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
//import org.springframework.stereotype.Repository;
//import reactor.core.publisher.Flux;
//
///**
// * <p>Title： LocationRepository </p>
// * <p>Description： </p>
// * <p>Company：ail </p>
// *
// * @author sujunxuan
// * @version V1.0
// * @date 2020/1/13 15:28
// */
//@Repository
//public interface LocationRepository extends ReactiveMongoRepository<Location, String> {
//
//    /**
//     * 根据车牌号查询定位
//     *
//     * @param vehicleNo
//     * @return
//     */
//    Flux<Location> findByVehicleNo(String vehicleNo);
//}
