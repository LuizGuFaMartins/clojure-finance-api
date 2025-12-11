(ns clojure-finance-api.domain)

(defn new-user [data]
  (merge {:id (random-uuid)
          :active true}
         data))