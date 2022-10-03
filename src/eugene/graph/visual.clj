(ns eugene.graph.visual
  "Functions for manipulation with visual representation of a graph."
  (:require [eugene.graph.core :as graph]))

(comment (def vctx
           {:graph          {:nodes            #{0 1 2}
                             :node->neighbours {0 #{1}
                                                1 #{2}}}
            :node->position {0 [100 100]
                             1 [150 100]
                             2 [200 250]}
            :selection      {:nodes [1]
                             :area  {:from [0 0]
                                     :to   [100 100]}
                             :edges #{[0 1]
                                      [1 2]}}}))

(defn select-node [ctx node]
  (cond-> ctx
          (every? #(not= % node) (get-in ctx [:selection :nodes]))
          (update-in [:selection :nodes] (fnil conj []) node)))

(defn select-nodes [ctx nodes]
  (reduce select-node ctx nodes))

(defn select-edge [ctx edge]
  (update-in ctx [:selection :edges] (fnil conj #{}) edge))

(defn select-edges [ctx edges]
  (reduce select-edge ctx edges))

(defn deselect-edge [ctx edge]
  (update-in ctx [:selection :edges] disj edge))

(defn deselect-node [ctx node]
  (update-in ctx [:selection :nodes] (partial filterv #(not= % node))))

(defn deselect-everything [ctx]
  (assoc ctx :selection {}))

(defn select-objects-in-area [{:keys [node->position graph] :as ctx}
                              [from-x from-y]
                              [to-x to-y]]
  (let [upper-left [(min from-x to-x) (min from-y to-y)]
        lower-right [(max from-x to-x) (max from-y to-y)]
        node-pos-in-selected-area? (fn [[_node-id node-pos]]
                                     (every? identity (map <= upper-left node-pos lower-right)))
        nodes-to-be-selected (->> node->position
                                  (filter node-pos-in-selected-area?)
                                  (map first))
        edges-to-be-selected (graph/edges graph (set nodes-to-be-selected))]
    (-> ctx
        (select-nodes nodes-to-be-selected)
        (select-edges edges-to-be-selected))))

(defn selected-nodes [ctx]
  (get-in ctx [:selection :nodes]))

(defn selected-edges [ctx]
  (get-in ctx [:selection :edges]))

(defn move-node [ctx delta node]
  (update-in ctx [:node->position node] #(mapv + delta %)))

(defn move-nodes [ctx delta nodes]
  (reduce #(move-node %1 delta %2) ctx nodes))

(comment input )

(defn create-node [ctx pos]
  (let [[graph new-node-id] (graph/add-node (:graph ctx))]
    (-> ctx
        (assoc :graph graph)
        (assoc-in [:node->position new-node-id] pos))))

(defn remove-node [ctx node]
  (-> ctx
      (update :node->position dissoc node)
      (update :graph graph/remove-node node)))

(defn remove-nodes [ctx nodes]
  (reduce remove-node ctx nodes))