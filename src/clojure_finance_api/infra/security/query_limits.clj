(ns clojure-finance-api.infra.security.query-limits
  (:require [io.pedestal.interceptor :refer [interceptor]]
            [io.pedestal.interceptor.chain :as chain]
            [com.walmartlabs.lacinia.executor :as executor]
            [com.walmartlabs.lacinia.selection :as selection]))

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


(defn calculate-complexity
  [selection-set]
  (let [
        description (executor/selection selection-set)
        field-def   (:field-definition description)
        base-complexity (or (:complexity field-def) 1)
        sub-selections (selection/selections selection-set)]
    (if (empty? sub-selections)
      base-complexity
      (+ base-complexity (reduce + (map #(calculate-complexity %) sub-selections))))))

(defn max-complexity-interceptor [schema limit]
  (interceptor
    {:name ::max-complexity
     :enter (fn [ctx]
              (let [
                    schemas schema
                    prepared-query (get-in ctx [:request :parsed-lacinia-query])
                    root-selections (:selections prepared-query)
                    complexity 10]

                (if (> complexity limit)
                  (-> ctx
                      (assoc :response
                             {:status 400
                              :body {:errors [{:message "Query with very high cost."
                                               :extensions {:cost complexity
                                                            :limit limit}}]}})
                      chain/terminate)
                  ctx)))}))

;(defn secure-query-executor-handler [schema complexity-limit depth-limit]
;  (interceptor
;    {:name ::secure-executor
;     :enter (fn [ctx]
;              (let [{:keys [graphql-query graphql-vars lacinia-app-context]} (:request ctx)
;                    options {:max-complexity complexity-limit :max-depth depth-limit}
;                    result (lacinia/execute
;                             schema
;                             graphql-query
;                             graphql-vars
;                             lacinia-app-context
;                             options)]
;                (assoc ctx :response {:status 200 :body result})))}))



