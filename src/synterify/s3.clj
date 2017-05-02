(ns synterify.s3
  (:require [aws.sdk.s3 :as s3]
            [digest :as digest]))

(def cred {:access-key (System/getenv "S3_KEY")
           :secret-key (System/getenv "S3_SECRET")})

(def bucket "synterify-images")

(def have-creds? (and
                   (:access-key cred)
                   (:secret-key cred)))

(when have-creds?
  (s3/create-bucket cred bucket))

(defn- get-key-by-url
  [url overlay-name]
  (digest/sha-256 (str url "##" overlay-name)))

(defn put-image
  [url file overlay-name]
  (when have-creds?
    (let [key (get-key-by-url url overlay-name)]
      (s3/put-object cred bucket key file)
      (s3/update-object-acl cred bucket key (s3/grant :all-users :read)))))

(defn image-exists?
  [url overlay-name]
  (if have-creds?
    (s3/object-exists? cred bucket (get-key-by-url url overlay-name))
    false))

(defn get-image
  [url overlay-name]
  (when have-creds?
    (:content
      (s3/get-object cred bucket (get-key-by-url url overlay-name)))))

