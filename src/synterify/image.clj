(ns synterify.image
  (:require [clojure.java.io :as io])
  (:import
    (java.awt.image BufferedImage)
    (javax.imageio ImageIO)
    (java.awt Graphics)
    (java.io ByteArrayOutputStream File)
    (java.net URL)))

(def overlays {:synterify    (io/resource "synterionkalja.png")
               :batmanterify (io/resource "batmantero.png")})

(defn- get-image-size
  [buffered-image]
  {:x (.getWidth buffered-image)
   :y (.getHeight buffered-image)})

(defn- map-values
  [f m]
  (into {} (for [[k v] m] [k (f v)])))

(defn- get-output-size
  [overlay image]
  (let [overlay-size (get-image-size overlay)
        image-size (get-image-size image)
        output-height (Math/min
                        (:y overlay-size)
                        (:y image-size))
        overlay-scale-factor (/ output-height
                                (:y overlay-size))
        image-scale-factor (/ output-height
                              (:y image-size))
        output-width (Math/max
                       (int (* overlay-scale-factor
                               (:x overlay-size)))
                       (int (* image-scale-factor
                               (:x image-size))))
        overlay-scaled-size (map-values (partial * overlay-scale-factor) overlay-size)
        image-scaled-size (map-values (partial * image-scale-factor) image-size)]
    {:x                   output-width
     :y                   output-height
     :overlay-pos-x       (- output-width (:x overlay-scaled-size))
     :overlay-pos-y       0
     :overlay-scaled-size overlay-scaled-size
     :image-scaled-size   image-scaled-size}))

(defn- combine-images
  [image overlay output-image-size]
  (let [output-image (BufferedImage.
                       (:x output-image-size)
                       (:y output-image-size)
                       BufferedImage/TYPE_INT_RGB)
        output-graphics (.getGraphics output-image)
        output-file (File/createTempFile "synterify-" ".jpg")]
    (.drawImage output-graphics
                image 0 0
                (get-in output-image-size [:image-scaled-size :x])
                (get-in output-image-size [:image-scaled-size :y])
                nil)
    (.drawImage output-graphics
                overlay (:overlay-pos-x output-image-size) (:overlay-pos-y output-image-size)
                (get-in output-image-size [:overlay-scaled-size :x])
                (get-in output-image-size [:overlay-scaled-size :y])
                nil)
    (println "writing to" (.getPath output-file))
    (ImageIO/write output-image "jpeg" output-file)
    output-file))

(defn combinate-bytes
  [image image-url-str overlay-name]
  (println "combining bytes" image-url-str overlay-name)
  (time
    (let [overlay (ImageIO/read (overlay-name overlays))
          image (ImageIO/read image)
          output-image-size (get-output-size overlay image)]
      (combine-images image overlay output-image-size))))

(defn combinate-url
  [image-url-str overlay-name]
  (println "combining" image-url-str overlay-name)
  (time
    (let [image-url (URL. image-url-str)
          overlay (ImageIO/read (overlay-name overlays))
          image (ImageIO/read image-url)
          output-image-size (get-output-size overlay image)]
      (combine-images image overlay output-image-size))))
