(ns clojure-finance-api.infra.security.query-limits
  (:require [io.pedestal.interceptor :refer [interceptor]]
            [io.pedestal.interceptor.chain :as chain]
            [com.walmartlabs.lacinia.constants :as constants]
            [com.walmartlabs.lacinia.selection :as sel]))

(defn- calculate-depth
  [selection-set]
  (if (empty? selection-set)
    0
    (let [depths (map #(calculate-depth (:selections %)) selection-set)]
      (inc (apply max 0 depths)))))

(defn max-depth-interceptor [limit]
  (interceptor
    {:name ::max-depth
     :enter (fn [ctx]
              (let [prepared-query (get-in ctx [:request :parsed-lacinia-query])
                    root-selections (:selections prepared-query)
                    depth (calculate-depth root-selections)]

                (if (> depth limit)
                  (-> ctx
                      (assoc :response
                             {:status 400
                              :headers {"Content-Type" "application/json"}
                              :body {:errors [{:message (str "Query too complex")
                                               :extensions {:current_depth depth
                                                            :max_depth limit}}]}})
                      chain/terminate)
                  ctx)))}))

(defn max-complexity-interceptor [limit]
  (interceptor
    {:name ::max-complexity
     :enter (fn [ctx]
              (let [
                    prepared-query (get-in ctx [:request constants/parsed-query-key])
                    actual-complexity (.getComplexity prepared-query)]

                (if (> actual-complexity limit)
                  (-> ctx
                      (assoc :response
                             {:status 400
                              :body {:errors [{:message "Query muito complexa."
                                               :extensions {:cost actual-complexity
                                                            :limit limit}}]}})
                      chain/terminate)
                  ctx)))}))
