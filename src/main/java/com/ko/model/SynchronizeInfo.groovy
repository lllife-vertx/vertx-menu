package com.ko.model

/**
 * Created by recovery on 1/31/14.
 */
class SynchronizeInfo extends BaseEntity<SynchronizeInfo> {
    int numberOfRecords;

    def static SynchronizeInfo $getLast(){

        def last = new SynchronizeInfo(lastUpdate: new Date(0L))
        List<SynchronizeInfo> syncs = $findAll(SynchronizeInfo.class)
        if (syncs.size() != 0) {
            last = syncs.last()
        }

        return last
    }

    def static List<CategoryInfo> $getUnSyncCategories(boolean all) {
        if(all) {
            return $findAll(CategoryInfo.class)
        }
        def last = $getLast()
        def cq = _connector.datastore.createQuery(CategoryInfo.class)
        def lasts = cq.field("lastUpdate").greaterThanOrEq(last.lastUpdate).field("delete").equal(false).fetch().iterator().toList()

        lasts.each { d -> d.identifier = d._id.toString() }
        return lasts
    }

    def static List<ProductInfo> $getUnSyncProducts(boolean  all) {
        if(all){
            return $findAll(ProductInfo)
        }
        def last = $getLast()
        def pq = _connector.datastore.createQuery(ProductInfo.class)
        def lasts = pq.field("lastUpdate").greaterThanOrEq(last.lastUpdate).field("delete").equal(false).fetch().iterator().toList()
        lasts.each { d -> d.identifier = d._id.toString() }

        return lasts
    }
}
