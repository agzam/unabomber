(ns unabomber.frontend.home
  (:require
   [re-frame.core :as rf :refer [dispatch
                                 reg-event-fx
                                 reg-event-db
                                 reg-sub
                                 subscribe
                                 inject-cofx]]
   [unabomber.frontend.http-xhrio]))

(reg-event-fx ::gb-search
  (fn [{:keys [db]} [_ val]]
    {:db (assoc db ::current-search-term val
                ::gb-data nil)
     :http-xhrio+ {:uri "/search"
                   :params {:term val}
                   :on-failure [::gb-search-failure]
                   :on-success [::gb-search-success]}}))

(reg-event-db ::gb-search-success
  (fn [db [_ data]]
    (assoc db ::gb-data data)))

(reg-sub ::gb-data #(get % ::gb-data))

(defn page-title []
  [:h1 {:class '[m-10]}
   "Giantbomb API Search"])

(defn search-bar []
  [:div {:class '[m-10]}
   [:div.relative
    [:input#default-search
     {:class '[block w-full p-4 pl-10 text-sm text-gray-900 border border-gray-300 bg-gray-50
               "dark:bg-gray-700"
               "dark:border-gray-600"
               "dark:placeholder-gray-400"
               "dark:text-white"
               "dark:focus:ring-orange-500"
               "dark:focus:border-orange-500"]
      :type "search"
      :placeholder "Search term"
      :on-change #(dispatch [::gb-search (-> % .-target .-value)])}]
    [:button
     {:class '[text-white absolute right-2 5 bottom-2 5 bg-orange-500
               font-medium text-sm px-4 py-2
               "hover:bg-orange-700"
               "focus:ring-4"
               "focus:outline-none"
               "focus:ring-orange-300"
               "dark:bg-orange-600"
               "dark:hover:bg-orange-700"
               "dark:focus:ring-orange-800"]
      :type "submit"}
     "Search"]]])

(defn html-str->html [html-str]
  [:div
   {:dangerouslySetInnerHTML #js {:__html html-str}}])

(defn gb-data-row [{:keys [image name deck description]
                    :as _gb-item}]
  [:<>
   [:tr
    [:td {:class '[border align-top]}
     [:img {:class '[m-5
                     w-30
                     max-w-30
                     max-h-30
                     min-w-30]
            :src (-> image :thumb_url)}]]
    [:td {:class '[border align-top p-5]}
     [:h2 name]]
    [:td {:class '[border align-top]} (html-str->html (or description deck))]]])

(defn results-table []
  (when-let [data @(subscribe [::gb-data])]
    [:div {:class '[relative overflow-x-auto m-10]}
     [:table {:class '[table-fixed w-full text-sm text-left
                       border
                       text-gray-500
                       "dark:text-gray-400"]}
      [:thead {:class '[text-xs text-gray-700
                        uppercase bg-gray-50
                        "dark:bg-gray-700"
                        "dark:text-gray-400"]}
       [:tr
        [:th {:class '[pl-3] :scope :col} " "]
        [:th {:class '[px-6 py-3] :scope :col} "Name"]
        [:th {:class '[px-6 py-3] :scope :col} "Description"]]]
      [:tbody
       (for [{:keys [id] :as row} data]
         ^{:key id}
         [gb-data-row row])]]]))

(defn view []
  [:div
   [page-title]
   [search-bar]
   [results-table]])
