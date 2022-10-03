(ns eugene.graph.core
  "Functions for manipulation with structure of a graph.")

(comment (def graph {:nodes            #{0 1 2}
                     ;; for the undirected graphs edge between nodes 'a' and 'b'
                     ;; will be stored as mapping (min a b) -> (max a b)
                     :node->neighbours {0 #{1}
                                        1 #{2}}
                     :directed?        false}))

(defn add-node [graph]
  (let [new-node-id (inc (reduce max -1 (:nodes graph)))
        updated-graph (update graph :nodes (fnil conj #{}) new-node-id)]
    [updated-graph new-node-id]))

(defn edge [graph node other-node]
  (if (:directional? graph)
    [node other-node]
    (vec (sort [node other-node]))))

(defn add-edge [graph [from to :as _edge]]
  ;; TODO: check if nodes are in the graph?
  (update-in graph [:node->neighbours from] (fnil conj #{}) to))

(defn add-edges [graph edges]
  (reduce add-edge graph edges))

(defn remove-edge [graph [from to :as _edge]]
  (update-in graph [:node->neighbours from] disj to))

(defn remove-edges [graph edges]
  (reduce remove-edge graph edges))

(defn remove-node [graph node]
  (-> graph
      (update :nodes disj node)
      (update :node->neighbours dissoc node)
      (update :node->neighbours update-vals #(disj % node))))

(defn edges
  ([graph]
   (edges graph any? any?))
  ([graph node-predicate]
   (edges graph node-predicate node-predicate))
  ([graph from-predicate to-predicate]
   (let [node->edges-from-it (fn [[node neighbours]]
                               (->> neighbours
                                    (filter to-predicate)
                                    (map #(vector node %))))]
     (->> graph
          :node->neighbours
          (filter (comp from-predicate first))
          (mapcat node->edges-from-it)))))