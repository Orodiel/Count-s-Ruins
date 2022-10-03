(ns eugene.graph.editor.actions
  "Various user actions with meta on how they should modify history."
  (:require [eugene.graph.core :as graph]
            [eugene.graph.visual :as visual]))

(defn select-node {:history-advancement :on-change} [state node expand?]
  (cond-> state
          (not expand?) visual/deselect-everything
          :always (visual/select-node node)))

(defn select-edge {:history-advancement :on-change} [state edge expand?]
  (cond-> state
          (not expand?) visual/deselect-everything
          :always (visual/select-edge edge)))

(defn deselect-objects {:history-advancement :on-change} [state]
  (visual/deselect-everything state))

(defn move-selected-objects {:history-advancement :stack} [state shift]
  (->> state
       visual/selected-nodes
       (visual/move-nodes state shift)))

(defn create-node {:history-advancement :always} [state position]
  (visual/create-node state position))

(defn connect-selected-nodes {:history-advancement :on-change} [state]
  (let [selected-nodes (visual/selected-nodes state)
        graph (:graph state)
        edges-to-be-added (map #(graph/edge graph %1 %2) selected-nodes (rest selected-nodes))]
    (update state :graph graph/add-edges edges-to-be-added)))

(defn delete-selected-objects {:history-advancement :on-change} [state]
  (let [selected-nodes (visual/selected-nodes state)
        selected-edges (visual/selected-edges state)]
    (-> state
        (visual/deselect-everything)
        (visual/remove-nodes selected-nodes)
        (update :graph graph/remove-edges selected-edges))))