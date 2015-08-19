(ns cljs-douban-reframe.views
    (:require [re-frame.core :as re-frame]
              [dommy.core :as dom :refer-macros [sel1 sel]])
    (:require-macros [reagent.ratom :refer [reaction]]))

(defn channel-list-div []
  (let [channel-list (re-frame/subscribe [:channel-list])
        current-channel-id (re-frame/subscribe [:current-channel-id])]
    (fn []
      #_(.log js/console (str "channel list " @channel-list))
      (let [select-id @current-channel-id
            channels @channel-list]
        [:div
         [:ul.channel-list
          (for [channel channels]
            ^{:key (channel "channel_id")}
            [:li {:channel-id (channel "channel_id")}
             (channel "name")])]]))))

(defn song-player []
  "display songs and controls"
  (let [current-song (re-frame/subscribe [:current-song])
        current-song-url (reaction (and @current-song (@current-song "url")))]
    (with-meta
      (fn []
        #_(.log js/console (str "Song-player render " (deref (re-frame/subscribe [:db]))))
        [:div#player
         [:p "title: " (and @current-song (@current-song "title"))]
         [:p "Like: " (str (and @current-song (@current-song "like")))]
         [:audio#player-audio
          {:autoPlay "true"
           :controls "true"
           :src (str @current-song-url)}]
         [:input {:type "button"
                  :value "Next Song"
                  :on-click #(re-frame/dispatch [:next-song])}]
         [:input {:type "button"
                  :value "Play/Stop"
                  :on-click #(let [player (sel1 :#player-audio)]
                               (if (.-paused player)
                                 (.play player)
                                 (.pause player)))}]
         [:input {:type "button"
                  :value "Rate Song"
                  :on-click #(re-frame/dispatch [:rate-song])}]
         [:input {:type "button"
                  :value "Delete Song"
                  :on-click #(re-frame/dispatch [:delete-song])}]])
      {:component-did-mount
       (fn [this]
         (dom/listen! (sel1 :#player-audio) :ended #(re-frame/dispatch [:end-song])))})))

(defn main-panel []
  (let [name (re-frame/subscribe [:name])]
    (fn []
      [:div#main
       [channel-list-div]
       [song-player]])))
