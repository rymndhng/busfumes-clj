(ns chat.map
  (:require [clojure.data.xml :as xml]
            [clojure.zip :as zip]
            [clojure.data.zip.xml :as zx]
            [clojure.java.io :as io])
  (:use clojure.test))

(defn generate-svg
  "Given a KML file, generates a SVG for drawing on the frontend with
  blended with bus stops."
  [kmlfile])


(defn kmz-to-map
  "Takes a kmz file and extracts the bus information as a map."
  [zipfile]
  (let [entry (first (enumeration-seq (.entries zipfile)))
        stream (.getInputStream zipfile entry)]
    (xml/parse stream)))


(defn extract-path
  "Given a ziplist from our KML, extract the coordinate nodes and
  flatten into a clojure list."
  [ziplist]
  (let [coords (zx/xml-> ziplist
                         :Document :Folder :Placemark
                         :MultiGeometry :LineString :coordinates)]
    (map (comp :content zip/node) coords)))


(def get-points-from-kml-map
  "Takes a kml map and retrieves the coordinates in a list."
  (comp extract-path zip/xml-zip kmz-to-map))

;;; Reading from a variety of sources

(def )

;; scratch for reading files off the web
(with-open [stream (clojure.java.io/input-stream
                    "http://nb.translink.ca/geodata/trip/027-NB5.kmz")
            zipStream (java.util.zip.ZipInputStream. stream)]
  (let [entry (.getNextEntry zipStream)
        file ]))

(def zipfile
  (with-open [rdr (clojure.java.io/reader "http://nb.translink.ca/geodata/trip/027-NB5.kmz")
              zipStream (java.util.zip.ZipInputStream. rdr)]
    ()
    ))

(def zipfile
  (java.util.zip.ZipFile. (clojure.java.io/as-url
                           )))
                                        ;
Some tests
(def testfile "/Users/rayh/Downloads/027-NB5.kmz")
(def contents (slurp testfile))
;; not ideal this loads an entire string... then what (?)


;;; Try using java inter-op for reading zips
(def zipfile (java.util.zip.ZipFile. "/Users/rayh/Downloads/027-NB5.kmz"))


                                        ;(def testkmz (java.util.zip.ZipFile. kmzfile))

(deftest kmz_equal
  (is (= 2 (+ 1 1))) "2 is 1 plus 1")


                                        ; (run-tests 'chat.map)
