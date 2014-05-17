(ns chat.models.db
    (:require [monger.core :as mg]
              [monger.collection :as mc]
              [monger.operators :refer :all]
              [monger.joda-time]
              [monger.json]
              [clj-time.core :as t]
              [clj-time.coerce :as te]))

;; Tries to get the Mongo URI from the environment variable
;; MONGOHQ_URL, otherwise default it to localhost
(let [uri (get (System/getenv) "MONGOHQ_URL" "mongodb://127.0.0.1/busfumes")]
  (mg/connect-via-uri! uri))

(defn get-raw-estimates-on-day
  " Takes a start joda time and gets all the busEstimateAggreates on that day plus one day"
  [start]
  (let [end (t/plus start (t/days 1))]
    (mc/find-maps "busEstimateAggregate" {"_id.ts" {$gte start $lt end}})))


(defn format-estimates
  " Takes the raw records from the estimates and prepares it for outputting"
  [raw-estimates]
  (->> raw-estimates
       (map #(apply assoc {} [:stopNo (-> % :_id :stopNo)
                              :ts     (-> % :_id :ts)
                              :avg    (-> % :value :avg)]))
       (group-by #(:stopNo %))))


(defn get-estimates-on-day
  " Gets the estimates and formats the records all at once"
  [day]
  (format-estimates (get-raw-estimates-on-day day)))

(get-estimates-on-day (t/today-at-midnight))


(defn get-route-stops
  "Takes a numeric route number and finds the ordered routeNo. Drops repeated routes"
  [routeNo]
  (->> (mc/find-maps "routes" {:routeNo (format "%03d" routeNo)})
       (map :stops)
       (apply concat)
       distinct))


(defn get-route-stops-metadata
  "Gets the stop metadata of routeNo ordered by the order they are traversed in"
  [routeNo]
  (let [routeOrder (get-route-stops routeNo)
        routeIndex (zipmap routeOrder (iterate inc 1))
        routeMeta  (mc/find-maps "stops" {:stopNo {$in routeOrder}})]
    (sort-by #(routeIndex (:stopNo %)) routeMeta)))

