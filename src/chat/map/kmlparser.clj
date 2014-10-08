(ns chat.map
  (:require [clojure.data.xml :as xml]
            [clojure.zip :as zip]
            [clojure.data.zip.xml :as zx]
            [clojure.java.io :as io]))

;; TODO: implement this!
(defn generate-svg
  "Given a KML file, generates a SVG for drawing on the frontend with
  blended with bus stops."
  [kmlfile])


(defn zipfile-streams
  "Takes a zipfile, and returns a lazy collection of the input
  streams.

  Users are expected to close the returned input streams."
  ([zipfile] (zipfile-streams zipfile (enumeration-seq (.entries zipfile))))
  ([zipfile entries]
     (if (empty? entries)
       (empty entries)
       (cons (apply xml/parse #(.getInputStream zipfile %) first entries)
             (lazy-seq (zipfile-streams zipfile (rest entries)))))))

(defn extract-bus-path
  "Given a ziplist from our KML, extract the coordinate nodes and
  flatten into a clojure list."
  [ziplist]
  (let [coords (zx/xml-> ziplist
                         :Document :Folder :Placemark
                         :MultiGeometry :LineString :coordinates)]
    (map (comp :content zip/node) coords)))


(defn retrieve-resource
  "Retrives the contents to a temporary file unless it already exists
  in the filesystem. Returns the file handle"
  [path-to-contents]
  (let [file (clojure.java.io/file path-to-contents)]
    (if (.exists file)
      ;; simply return the file if it exists
      file

      ;; otherwise create a temporary file
      (let [tmpfile (java.io.File/createTempFile "busfumes" ".kmz")]
        (with-open [reader (clojure.java.io/input-stream path-to-contents)
                    writer (clojure.java.io/output-stream tmpfile)]
          (clojure.java.io/copy reader writer))
        tmpfile))))

;;; Should Retrive 971 bytes
;; (retrieve-path-to-tmpfile "http://nb.translink.ca/geodata/trip/027-NB5.kmz")


;;; Expose this as a public method
(defn get-points-from-kmz-map
  "Takes a path to a kmz, taking the first (and hopefully only entry)
  and retrieves the coordinates in a list."
  [path]
  (with-open [stream (first (-> (retrieve-resource path)
                                 java.util.zip.ZipFile.
                                 zipfile-streams))]
    (-> stream xml/parse zip/xml-zip extract-bus-path)))

;; (get-points-from-kml-map "http://nb.translink.ca/geodata/trip/027-NB5.kmz")
