(ns eugene.graph.editor.subs
  (:require [cljfx.api :as fx]
            [eugene.graph.core :as graph]))

(defn current-state [context]
  (fx/sub-val context get-in [:states (fx/sub-val context :current)]))

(defn nodes [context]
  (let [current-state (fx/sub-ctx context current-state)]
    (get-in current-state [:graph :nodes])))

(defn edges [context]
  (let [{:keys [graph]} (fx/sub-ctx context current-state)]
    (graph/edges graph)))

(defn selected-nodes [context]
  (let [current-state (fx/sub-ctx context current-state)]
    (set (get-in current-state [:selection :nodes]))))

(defn selected-edges [context]
  (let [current-state (fx/sub-ctx context current-state)]
    (get-in current-state [:selection :edges])))

(defn node-position [context id]
  (let [current-state (fx/sub-ctx context current-state)]
    (get-in current-state [:node->position id])))
