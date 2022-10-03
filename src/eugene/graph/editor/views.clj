(ns eugene.graph.editor.views
  (:require [cljfx.api :as fx]
            [cljfx.css :as css]
            [cljfx.ext.node :as fx.ext.node]
            [eugene.graph.editor.subs :as subs]))

(def style
  (css/register ::style
                (let [stroke-width 5
                      background-color "#FFEEE6"
                      selection-color "#FCB1A7"
                      default-color "#AECFBA"]
                  {".graph"    {"-node"           {:-fx-stroke-width stroke-width}
                                "-edge"           {:-fx-stroke-width stroke-width}
                                "-selection-area" {:-fx-fill              nil
                                                   :-fx-stroke            selection-color
                                                   :-fx-stroke-width      1
                                                   :-fx-stroke-dash-array [1 4]}
                                "-editor"         {:-fx-background-color background-color}}
                   ":selected" {:-fx-stroke selection-color
                                :-fx-fill   selection-color}
                   ":normal"   {:-fx-stroke default-color
                                :-fx-fill   default-color}})))

(defn node-view [{:keys [fx/context id]}]
  (let [selected? ((fx/sub-ctx context subs/selected-nodes) id)
        [x y] (fx/sub-ctx context subs/node-position id)]
    {:fx/type          :circle
     :style-class      ["graph-node" (when selected? ":selected")]
     :pseudo-classes   #{(if selected? :selected :normal)}
     :radius           20
     :center-x         x
     :center-y         y
     :on-mouse-pressed {:event/type :on-object-pressed, :object-type :node, :id id}
     :on-mouse-dragged {:event/type :on-object-dragged}}))

(defn edge-view [{:keys [fx/context id]}]
  (let [selected? (get (fx/sub-ctx context subs/selected-edges) id)
        [[from-x from-y] [to-x to-y]] (map #(fx/sub-ctx context subs/node-position %) id)]
    {:fx/type          :line
     :style-class      ["graph-edge" (when selected? ":selected")]
     :pseudo-classes   #{(if selected? :selected :normal)}
     :start-x          from-x
     :start-y          from-y
     :end-x            to-x
     :end-y            to-y
     :on-mouse-pressed {:event/type :on-object-pressed, :object-type :edge, :id id}}))

(defn history-slider-view [{:keys [fx/context]}]
  (let [current-state-index (fx/sub-val context :current)
        last-state-index (count (fx/sub-val context :states))]
    {:fx/type fx.ext.node/with-tooltip-props
     :props   {:tooltip {:fx/type :tooltip
                         :text    "Slide to browse history"}}
     :desc    {:fx/type          :slider
               :pref-width       100
               :min              0
               :max              (dec last-state-index)
               :major-tick-unit  1
               :minor-tick-count 0
               :value            current-state-index
               :show-tick-labels true
               ;; TODO: would be nice if we could annotate ticks with :last-op for each state
               ;; :tick-label-formatter
               :snap-to-ticks    true
               :show-tick-marks  true
               :on-value-changed {:event/type :perform-time-travel}}}))

(defn graph-editor-view [{:keys [fx/context]}]
  (let [nodes (for [id (fx/sub-ctx context subs/nodes)]
                {:fx/type node-view
                 :id      id})
        edges (for [id (fx/sub-ctx context subs/edges)]
                {:fx/type edge-view
                 :id      id})]
    {:fx/type     :v-box
     :style-class ["graph-editor"]
     :fill-width  true
     :children    [{:fx/type history-slider-view}
                   {:fx/type          :pane
                    :pref-height      1500
                    :pref-width       1000
                    :on-mouse-moved   {:event/type :on-mouse-moved}
                    :on-mouse-pressed {:event/type :on-pane-pressed}
                    :children         (concat edges nodes)}]}))

(defn root-view [_]
  {:fx/type       :stage
   :showing       true
   :always-on-top true
   :height        1500
   :width         1000
   :scene         {:fx/type        :scene
                   :stylesheets    [(::css/url style)]
                   :on-key-pressed {:event/type :on-key-pressed}
                   :root           {:fx/type graph-editor-view}}})

(comment (user/fx-help :pane)
         ((:renderer eugene.graph.editor.core/editor)))