(ns eugene.graph.editor.handlers
  "Interpretation of UI events and mapping to editor's actions."
  (:require [cljfx.api :as fx]
            [eugene.graph.editor.actions :as actions]
            [eugene.graph.editor.history :as history])
  (:import (javafx.scene.input MouseEvent KeyEvent)))

(set! *warn-on-reflection* true)

(defmulti event-handler :event/type)

(defn mouse-event->mouse-pos [^MouseEvent event]
  (when event
    [(int (.getX event))
     (int (.getY event))]))

(defmethod event-handler :on-mouse-moved [{:keys [fx/event]}]
  (when-let [pos (mouse-event->mouse-pos event)]
    {:mouse pos}))

(defn perform-action [context action-sym & args]
  (let [{:keys [history-advancement]} (meta action-sym)]
    {:context (fx/swap-context context
                               (fn [context]
                                 (apply history/advance
                                        context
                                        history-advancement
                                        action-sym
                                        args)))}))

(defmethod event-handler :on-object-dragged [{:keys [fx/event fx/context mouse]}]
  (let [new-pos (mouse-event->mouse-pos event)
        shift (map - new-pos mouse)]
    (-> context
        (perform-action #'actions/move-selected-objects shift)
        (assoc :mouse new-pos))))

(defmethod event-handler :perform-time-travel [{:keys [^Double fx/event fx/context]}]
  (let [target-index (Math/round event)]
    (when (not= target-index (fx/sub-val context :current))
      {:context (fx/swap-context context assoc :current target-index)})))

(defmethod event-handler :on-object-pressed [{:keys [fx/context ^MouseEvent fx/event object-type id]}]
  (let [action (case object-type
                 :node #'actions/select-node
                 :edge #'actions/select-edge)]
    (perform-action context action id (.isShiftDown event))))

(defmethod event-handler :on-pane-pressed [{:keys [fx/context ^MouseEvent fx/event]}]
  (when (= (->> event
                .getTarget
                .getClass
                .getName)
           "javafx.scene.layout.Pane")
    (perform-action context #'actions/deselect-objects)))

(defmethod event-handler :on-key-pressed [{:keys [fx/context ^KeyEvent fx/event mouse]}]
  (let [undo (fn [] {:context (fx/swap-context context history/undo)})
        redo (fn [] {:context (fx/swap-context context history/redo)})]
    (case (.getChar (.getCode event))
      "U" (undo)
      "R" (redo)
      "Z" (when (.isControlDown event) (undo))
      "Y" (when (.isControlDown event) (redo))
      "A" (perform-action context #'actions/create-node mouse)
      "C" (perform-action context #'actions/connect-selected-nodes)
      "D" (perform-action context #'actions/delete-selected-objects)
      nil)))

(defmethod event-handler :default [e]
  nil)