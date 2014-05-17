(ns chat.routes.cljsexample
  (:require [compojure.core :refer :all]
            [chat.views.layout :as layout]
            [chat.models.db :as busfumes]
            [clj-time.core :as t]
            [clj-time.format :as tf])
  (:import (org.joda.time DateTimeZone)))

(def messages
  (atom 
    [{:message "Hello world"
      :user    "Foo"}
     {:message "Ajax is fun"
      :user    "Bar"}]))

;; slightly higher order functions
(def pacific-fmt (tf/formatter "yyyy-MM-dd" (DateTimeZone/forID "Canada/Pacific")))

(defn delays
  "Takes string input of start-date-string in format 'yyyy-MM-dd' and a string routeNo
  and gets you the stuff you need"
  [start-date-string routeNo]
  (let [start (tf/parse pacific-fmt start-date-string)
        delays (busfumes/get-estimates-on-day start)
        metadata (busfumes/get-route-stops-metadata
                      (Integer/parseInt (re-find #"\A-?\d+" routeNo)))]
    {:delays delays :metadata (filter #(get delays (:stopNo %))  metadata)}))

(defroutes cljs-routes
  (GET "/delays/:routeNo/:date" {{routeNo :routeNo date :date} :params}
       {:body (delays date routeNo)})
  (GET "/cljsexample" [] (layout/render "cljsexample.html"))
  (GET "/messages" [] {:body @messages})
  (POST "/add-message" [message user]
        {:body (swap! messages conj {:message message :user user})}))
