(ns clojure-finance-api.infra.http.response
  (:require [cheshire.core :as json]))

(defn response
  ([status]
   (response status nil))
  ([status body]
   (merge
     {:status status
      :headers {"Content-Type" "application/json"}}
     (when body {:body (json/encode body)}))))

(defn response-error
  ([status message]
   (response status {:error message}))
  ([status message details]
   (response status {:error message :details details})))
