(ns cljs-douban-reframe.views
    (:require [re-frame.core :as re-frame]
              [dommy.core :as dom :refer-macros [sel1 sel]]))

(defn channel-list-div []
  (let [channel-list (re-frame/subscribe [:channel-list])
        current-channel-seq-id (re-frame/subscribe [:current-channel-seq-id])]
    (fn []
      (let [select-id @current-channel-seq-id]
        [:div
         [:ul.channel-list
          (for [channel @channel-list]
            ^{:key (channel "channel_id")}
            [:li {:class (if (= select-id (channel "seq_id")) "selected" "")}
             (channel "name")])]]))))

(defn song-player []
  "display songs and controls"
  (let [current-song (re-frame/subscribe [:current-song])]
    (with-meta
      (fn []
        [:div#player
         [:p "title: " (and @current-song (@current-song "title"))]
         [:audio#player-audio
          {:autoPlay "true"
           :controls "true"
           :src (str (and @current-song (@current-song "url")))}]
         [:input {:type "button"
                  :value "Next Song"
                  :on-click #(re-frame/dispatch [:next-song])}]
         [:input {:type "button"
                  :value "Play/Stop"
                  :on-click #(re-frame/dispatch [:pause-song (sel1 :#player-audio)])}]])
      {:component-did-mount
       (fn [this]
         (dom/listen! (sel1 :#player-audio) :ended #(re-frame/dispatch [:end-song])))})))

(defn tmp-player []
  (let [current-song (re-frame/subscribe [:current-song])]
    (fn []
        [:div#player
         [:p "title: " (@current-song "title")]]))) 

(defn main-panel []
  (let [name (re-frame/subscribe [:name])]
    (fn []
      [:div#main
       [channel-list-div]
       [song-player]])))
