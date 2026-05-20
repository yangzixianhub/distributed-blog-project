/*
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
$(document).ready(function() {

    $(".click-title").mouseenter( function(    e){
        e.preventDefault();
        this.style.cursor="pointer";
    });
    $(".click-title").mousedown( function(event){
        event.preventDefault();
    });

    // Ugly code while this script is shared among several pages
    try{
        refreshHitsPerSecond(true);
    } catch(e){}
    try{
        refreshResponseTimeOverTime(true);
    } catch(e){}
    try{
        refreshResponseTimePercentiles();
    } catch(e){}
});


var responseTimePercentilesInfos = {
        data: {"result": {"minY": 44.0, "minX": 0.0, "maxY": 3586.0, "series": [{"data": [[0.0, 44.0], [0.1, 45.0], [0.2, 46.0], [0.3, 46.0], [0.4, 47.0], [0.5, 48.0], [0.6, 48.0], [0.7, 49.0], [0.8, 49.0], [0.9, 49.0], [1.0, 50.0], [1.1, 50.0], [1.2, 50.0], [1.3, 50.0], [1.4, 50.0], [1.5, 50.0], [1.6, 50.0], [1.7, 51.0], [1.8, 51.0], [1.9, 51.0], [2.0, 51.0], [2.1, 51.0], [2.2, 51.0], [2.3, 52.0], [2.4, 52.0], [2.5, 52.0], [2.6, 52.0], [2.7, 52.0], [2.8, 52.0], [2.9, 52.0], [3.0, 52.0], [3.1, 53.0], [3.2, 53.0], [3.3, 53.0], [3.4, 53.0], [3.5, 53.0], [3.6, 53.0], [3.7, 54.0], [3.8, 54.0], [3.9, 54.0], [4.0, 54.0], [4.1, 54.0], [4.2, 54.0], [4.3, 55.0], [4.4, 55.0], [4.5, 55.0], [4.6, 55.0], [4.7, 55.0], [4.8, 55.0], [4.9, 56.0], [5.0, 56.0], [5.1, 56.0], [5.2, 56.0], [5.3, 56.0], [5.4, 56.0], [5.5, 57.0], [5.6, 57.0], [5.7, 57.0], [5.8, 57.0], [5.9, 58.0], [6.0, 58.0], [6.1, 58.0], [6.2, 58.0], [6.3, 58.0], [6.4, 58.0], [6.5, 58.0], [6.6, 58.0], [6.7, 59.0], [6.8, 59.0], [6.9, 59.0], [7.0, 59.0], [7.1, 59.0], [7.2, 60.0], [7.3, 60.0], [7.4, 61.0], [7.5, 61.0], [7.6, 62.0], [7.7, 62.0], [7.8, 62.0], [7.9, 63.0], [8.0, 63.0], [8.1, 64.0], [8.2, 64.0], [8.3, 64.0], [8.4, 65.0], [8.5, 65.0], [8.6, 65.0], [8.7, 65.0], [8.8, 66.0], [8.9, 67.0], [9.0, 67.0], [9.1, 68.0], [9.2, 68.0], [9.3, 68.0], [9.4, 69.0], [9.5, 69.0], [9.6, 70.0], [9.7, 70.0], [9.8, 71.0], [9.9, 71.0], [10.0, 72.0], [10.1, 72.0], [10.2, 73.0], [10.3, 76.0], [10.4, 78.0], [10.5, 78.0], [10.6, 79.0], [10.7, 81.0], [10.8, 85.0], [10.9, 87.0], [11.0, 87.0], [11.1, 88.0], [11.2, 90.0], [11.3, 92.0], [11.4, 92.0], [11.5, 96.0], [11.6, 102.0], [11.7, 113.0], [11.8, 120.0], [11.9, 132.0], [12.0, 141.0], [12.1, 159.0], [12.2, 171.0], [12.3, 201.0], [12.4, 210.0], [12.5, 211.0], [12.6, 211.0], [12.7, 213.0], [12.8, 214.0], [12.9, 214.0], [13.0, 215.0], [13.1, 215.0], [13.2, 216.0], [13.3, 217.0], [13.4, 217.0], [13.5, 217.0], [13.6, 218.0], [13.7, 218.0], [13.8, 218.0], [13.9, 219.0], [14.0, 219.0], [14.1, 220.0], [14.2, 220.0], [14.3, 220.0], [14.4, 220.0], [14.5, 221.0], [14.6, 221.0], [14.7, 221.0], [14.8, 221.0], [14.9, 221.0], [15.0, 222.0], [15.1, 222.0], [15.2, 222.0], [15.3, 222.0], [15.4, 222.0], [15.5, 222.0], [15.6, 223.0], [15.7, 223.0], [15.8, 223.0], [15.9, 223.0], [16.0, 223.0], [16.1, 223.0], [16.2, 223.0], [16.3, 224.0], [16.4, 224.0], [16.5, 224.0], [16.6, 224.0], [16.7, 225.0], [16.8, 225.0], [16.9, 225.0], [17.0, 225.0], [17.1, 225.0], [17.2, 225.0], [17.3, 225.0], [17.4, 226.0], [17.5, 226.0], [17.6, 226.0], [17.7, 226.0], [17.8, 226.0], [17.9, 226.0], [18.0, 226.0], [18.1, 227.0], [18.2, 227.0], [18.3, 227.0], [18.4, 227.0], [18.5, 227.0], [18.6, 227.0], [18.7, 227.0], [18.8, 227.0], [18.9, 227.0], [19.0, 228.0], [19.1, 228.0], [19.2, 228.0], [19.3, 228.0], [19.4, 228.0], [19.5, 228.0], [19.6, 228.0], [19.7, 229.0], [19.8, 229.0], [19.9, 229.0], [20.0, 229.0], [20.1, 229.0], [20.2, 229.0], [20.3, 229.0], [20.4, 229.0], [20.5, 229.0], [20.6, 229.0], [20.7, 229.0], [20.8, 229.0], [20.9, 229.0], [21.0, 230.0], [21.1, 230.0], [21.2, 230.0], [21.3, 230.0], [21.4, 230.0], [21.5, 230.0], [21.6, 230.0], [21.7, 230.0], [21.8, 230.0], [21.9, 230.0], [22.0, 230.0], [22.1, 231.0], [22.2, 231.0], [22.3, 231.0], [22.4, 231.0], [22.5, 231.0], [22.6, 231.0], [22.7, 231.0], [22.8, 231.0], [22.9, 231.0], [23.0, 231.0], [23.1, 231.0], [23.2, 231.0], [23.3, 232.0], [23.4, 232.0], [23.5, 232.0], [23.6, 232.0], [23.7, 232.0], [23.8, 232.0], [23.9, 232.0], [24.0, 232.0], [24.1, 232.0], [24.2, 232.0], [24.3, 232.0], [24.4, 232.0], [24.5, 232.0], [24.6, 233.0], [24.7, 233.0], [24.8, 233.0], [24.9, 233.0], [25.0, 233.0], [25.1, 233.0], [25.2, 233.0], [25.3, 233.0], [25.4, 233.0], [25.5, 233.0], [25.6, 233.0], [25.7, 234.0], [25.8, 234.0], [25.9, 234.0], [26.0, 234.0], [26.1, 234.0], [26.2, 234.0], [26.3, 234.0], [26.4, 234.0], [26.5, 234.0], [26.6, 235.0], [26.7, 235.0], [26.8, 235.0], [26.9, 235.0], [27.0, 235.0], [27.1, 235.0], [27.2, 235.0], [27.3, 235.0], [27.4, 235.0], [27.5, 236.0], [27.6, 236.0], [27.7, 236.0], [27.8, 236.0], [27.9, 236.0], [28.0, 236.0], [28.1, 236.0], [28.2, 236.0], [28.3, 236.0], [28.4, 236.0], [28.5, 236.0], [28.6, 236.0], [28.7, 236.0], [28.8, 237.0], [28.9, 237.0], [29.0, 237.0], [29.1, 237.0], [29.2, 237.0], [29.3, 237.0], [29.4, 237.0], [29.5, 237.0], [29.6, 237.0], [29.7, 238.0], [29.8, 238.0], [29.9, 238.0], [30.0, 238.0], [30.1, 238.0], [30.2, 238.0], [30.3, 238.0], [30.4, 238.0], [30.5, 238.0], [30.6, 239.0], [30.7, 239.0], [30.8, 239.0], [30.9, 239.0], [31.0, 239.0], [31.1, 239.0], [31.2, 239.0], [31.3, 239.0], [31.4, 239.0], [31.5, 239.0], [31.6, 239.0], [31.7, 240.0], [31.8, 240.0], [31.9, 240.0], [32.0, 240.0], [32.1, 240.0], [32.2, 240.0], [32.3, 240.0], [32.4, 240.0], [32.5, 240.0], [32.6, 240.0], [32.7, 241.0], [32.8, 241.0], [32.9, 241.0], [33.0, 241.0], [33.1, 241.0], [33.2, 241.0], [33.3, 241.0], [33.4, 241.0], [33.5, 241.0], [33.6, 241.0], [33.7, 241.0], [33.8, 242.0], [33.9, 242.0], [34.0, 242.0], [34.1, 242.0], [34.2, 242.0], [34.3, 242.0], [34.4, 242.0], [34.5, 242.0], [34.6, 242.0], [34.7, 243.0], [34.8, 243.0], [34.9, 243.0], [35.0, 243.0], [35.1, 243.0], [35.2, 243.0], [35.3, 243.0], [35.4, 243.0], [35.5, 243.0], [35.6, 243.0], [35.7, 243.0], [35.8, 243.0], [35.9, 243.0], [36.0, 244.0], [36.1, 244.0], [36.2, 244.0], [36.3, 244.0], [36.4, 244.0], [36.5, 244.0], [36.6, 244.0], [36.7, 244.0], [36.8, 244.0], [36.9, 244.0], [37.0, 244.0], [37.1, 244.0], [37.2, 244.0], [37.3, 244.0], [37.4, 245.0], [37.5, 245.0], [37.6, 245.0], [37.7, 245.0], [37.8, 245.0], [37.9, 245.0], [38.0, 245.0], [38.1, 245.0], [38.2, 245.0], [38.3, 246.0], [38.4, 246.0], [38.5, 246.0], [38.6, 246.0], [38.7, 246.0], [38.8, 246.0], [38.9, 246.0], [39.0, 246.0], [39.1, 247.0], [39.2, 247.0], [39.3, 247.0], [39.4, 247.0], [39.5, 247.0], [39.6, 247.0], [39.7, 247.0], [39.8, 247.0], [39.9, 247.0], [40.0, 247.0], [40.1, 247.0], [40.2, 248.0], [40.3, 248.0], [40.4, 248.0], [40.5, 248.0], [40.6, 248.0], [40.7, 248.0], [40.8, 248.0], [40.9, 248.0], [41.0, 248.0], [41.1, 249.0], [41.2, 249.0], [41.3, 249.0], [41.4, 249.0], [41.5, 249.0], [41.6, 249.0], [41.7, 249.0], [41.8, 249.0], [41.9, 249.0], [42.0, 249.0], [42.1, 249.0], [42.2, 250.0], [42.3, 250.0], [42.4, 250.0], [42.5, 250.0], [42.6, 250.0], [42.7, 250.0], [42.8, 250.0], [42.9, 250.0], [43.0, 251.0], [43.1, 251.0], [43.2, 251.0], [43.3, 251.0], [43.4, 251.0], [43.5, 251.0], [43.6, 251.0], [43.7, 251.0], [43.8, 252.0], [43.9, 252.0], [44.0, 252.0], [44.1, 252.0], [44.2, 252.0], [44.3, 253.0], [44.4, 253.0], [44.5, 253.0], [44.6, 253.0], [44.7, 253.0], [44.8, 253.0], [44.9, 254.0], [45.0, 254.0], [45.1, 254.0], [45.2, 254.0], [45.3, 254.0], [45.4, 254.0], [45.5, 254.0], [45.6, 254.0], [45.7, 255.0], [45.8, 255.0], [45.9, 255.0], [46.0, 255.0], [46.1, 255.0], [46.2, 255.0], [46.3, 255.0], [46.4, 255.0], [46.5, 256.0], [46.6, 256.0], [46.7, 256.0], [46.8, 256.0], [46.9, 256.0], [47.0, 256.0], [47.1, 256.0], [47.2, 256.0], [47.3, 257.0], [47.4, 257.0], [47.5, 257.0], [47.6, 257.0], [47.7, 257.0], [47.8, 258.0], [47.9, 258.0], [48.0, 258.0], [48.1, 258.0], [48.2, 259.0], [48.3, 259.0], [48.4, 259.0], [48.5, 259.0], [48.6, 259.0], [48.7, 259.0], [48.8, 259.0], [48.9, 260.0], [49.0, 260.0], [49.1, 260.0], [49.2, 260.0], [49.3, 260.0], [49.4, 260.0], [49.5, 261.0], [49.6, 261.0], [49.7, 261.0], [49.8, 261.0], [49.9, 261.0], [50.0, 261.0], [50.1, 261.0], [50.2, 262.0], [50.3, 262.0], [50.4, 262.0], [50.5, 262.0], [50.6, 263.0], [50.7, 263.0], [50.8, 263.0], [50.9, 263.0], [51.0, 263.0], [51.1, 263.0], [51.2, 263.0], [51.3, 263.0], [51.4, 264.0], [51.5, 264.0], [51.6, 264.0], [51.7, 264.0], [51.8, 264.0], [51.9, 265.0], [52.0, 265.0], [52.1, 265.0], [52.2, 265.0], [52.3, 265.0], [52.4, 265.0], [52.5, 266.0], [52.6, 266.0], [52.7, 266.0], [52.8, 266.0], [52.9, 267.0], [53.0, 267.0], [53.1, 267.0], [53.2, 267.0], [53.3, 267.0], [53.4, 267.0], [53.5, 268.0], [53.6, 268.0], [53.7, 268.0], [53.8, 268.0], [53.9, 268.0], [54.0, 268.0], [54.1, 269.0], [54.2, 269.0], [54.3, 269.0], [54.4, 270.0], [54.5, 270.0], [54.6, 270.0], [54.7, 271.0], [54.8, 271.0], [54.9, 271.0], [55.0, 271.0], [55.1, 272.0], [55.2, 272.0], [55.3, 272.0], [55.4, 273.0], [55.5, 273.0], [55.6, 273.0], [55.7, 274.0], [55.8, 274.0], [55.9, 275.0], [56.0, 275.0], [56.1, 275.0], [56.2, 275.0], [56.3, 276.0], [56.4, 276.0], [56.5, 276.0], [56.6, 277.0], [56.7, 277.0], [56.8, 278.0], [56.9, 278.0], [57.0, 278.0], [57.1, 278.0], [57.2, 279.0], [57.3, 279.0], [57.4, 279.0], [57.5, 279.0], [57.6, 279.0], [57.7, 280.0], [57.8, 280.0], [57.9, 280.0], [58.0, 281.0], [58.1, 281.0], [58.2, 281.0], [58.3, 282.0], [58.4, 282.0], [58.5, 282.0], [58.6, 283.0], [58.7, 283.0], [58.8, 284.0], [58.9, 284.0], [59.0, 285.0], [59.1, 285.0], [59.2, 285.0], [59.3, 286.0], [59.4, 286.0], [59.5, 287.0], [59.6, 288.0], [59.7, 288.0], [59.8, 289.0], [59.9, 289.0], [60.0, 290.0], [60.1, 291.0], [60.2, 291.0], [60.3, 291.0], [60.4, 292.0], [60.5, 293.0], [60.6, 294.0], [60.7, 294.0], [60.8, 294.0], [60.9, 294.0], [61.0, 294.0], [61.1, 295.0], [61.2, 296.0], [61.3, 296.0], [61.4, 297.0], [61.5, 297.0], [61.6, 298.0], [61.7, 298.0], [61.8, 299.0], [61.9, 299.0], [62.0, 299.0], [62.1, 300.0], [62.2, 300.0], [62.3, 300.0], [62.4, 301.0], [62.5, 301.0], [62.6, 301.0], [62.7, 302.0], [62.8, 302.0], [62.9, 303.0], [63.0, 305.0], [63.1, 305.0], [63.2, 306.0], [63.3, 307.0], [63.4, 308.0], [63.5, 309.0], [63.6, 309.0], [63.7, 310.0], [63.8, 311.0], [63.9, 311.0], [64.0, 312.0], [64.1, 314.0], [64.2, 315.0], [64.3, 315.0], [64.4, 316.0], [64.5, 317.0], [64.6, 318.0], [64.7, 318.0], [64.8, 319.0], [64.9, 321.0], [65.0, 321.0], [65.1, 322.0], [65.2, 323.0], [65.3, 324.0], [65.4, 326.0], [65.5, 326.0], [65.6, 328.0], [65.7, 329.0], [65.8, 330.0], [65.9, 331.0], [66.0, 333.0], [66.1, 334.0], [66.2, 335.0], [66.3, 339.0], [66.4, 340.0], [66.5, 341.0], [66.6, 341.0], [66.7, 343.0], [66.8, 344.0], [66.9, 345.0], [67.0, 348.0], [67.1, 350.0], [67.2, 354.0], [67.3, 361.0], [67.4, 365.0], [67.5, 366.0], [67.6, 367.0], [67.7, 368.0], [67.8, 373.0], [67.9, 377.0], [68.0, 380.0], [68.1, 381.0], [68.2, 382.0], [68.3, 383.0], [68.4, 387.0], [68.5, 391.0], [68.6, 398.0], [68.7, 399.0], [68.8, 402.0], [68.9, 404.0], [69.0, 407.0], [69.1, 410.0], [69.2, 413.0], [69.3, 425.0], [69.4, 436.0], [69.5, 441.0], [69.6, 447.0], [69.7, 452.0], [69.8, 469.0], [69.9, 471.0], [70.0, 472.0], [70.1, 483.0], [70.2, 508.0], [70.3, 535.0], [70.4, 558.0], [70.5, 565.0], [70.6, 592.0], [70.7, 599.0], [70.8, 607.0], [70.9, 622.0], [71.0, 634.0], [71.1, 651.0], [71.2, 671.0], [71.3, 674.0], [71.4, 702.0], [71.5, 708.0], [71.6, 718.0], [71.7, 726.0], [71.8, 730.0], [71.9, 739.0], [72.0, 745.0], [72.1, 756.0], [72.2, 758.0], [72.3, 765.0], [72.4, 770.0], [72.5, 771.0], [72.6, 772.0], [72.7, 785.0], [72.8, 804.0], [72.9, 808.0], [73.0, 820.0], [73.1, 821.0], [73.2, 850.0], [73.3, 879.0], [73.4, 911.0], [73.5, 924.0], [73.6, 941.0], [73.7, 953.0], [73.8, 967.0], [73.9, 982.0], [74.0, 1008.0], [74.1, 1030.0], [74.2, 1051.0], [74.3, 1053.0], [74.4, 1073.0], [74.5, 1088.0], [74.6, 1090.0], [74.7, 1091.0], [74.8, 1095.0], [74.9, 1101.0], [75.0, 1124.0], [75.1, 1137.0], [75.2, 1145.0], [75.3, 1162.0], [75.4, 1166.0], [75.5, 1168.0], [75.6, 1170.0], [75.7, 1173.0], [75.8, 1177.0], [75.9, 1179.0], [76.0, 1181.0], [76.1, 1183.0], [76.2, 1191.0], [76.3, 1198.0], [76.4, 1208.0], [76.5, 1224.0], [76.6, 1226.0], [76.7, 1227.0], [76.8, 1228.0], [76.9, 1228.0], [77.0, 1230.0], [77.1, 1235.0], [77.2, 1236.0], [77.3, 1237.0], [77.4, 1238.0], [77.5, 1238.0], [77.6, 1238.0], [77.7, 1239.0], [77.8, 1239.0], [77.9, 1240.0], [78.0, 1243.0], [78.1, 1257.0], [78.2, 1262.0], [78.3, 1267.0], [78.4, 1273.0], [78.5, 1289.0], [78.6, 1290.0], [78.7, 1290.0], [78.8, 1291.0], [78.9, 1299.0], [79.0, 1311.0], [79.1, 1324.0], [79.2, 1331.0], [79.3, 1333.0], [79.4, 1350.0], [79.5, 1359.0], [79.6, 1362.0], [79.7, 1374.0], [79.8, 1391.0], [79.9, 1393.0], [80.0, 1396.0], [80.1, 1397.0], [80.2, 1411.0], [80.3, 1436.0], [80.4, 1445.0], [80.5, 1450.0], [80.6, 1453.0], [80.7, 1455.0], [80.8, 1458.0], [80.9, 1464.0], [81.0, 1468.0], [81.1, 1472.0], [81.2, 1475.0], [81.3, 1479.0], [81.4, 1485.0], [81.5, 1490.0], [81.6, 1497.0], [81.7, 1500.0], [81.8, 1503.0], [81.9, 1508.0], [82.0, 1511.0], [82.1, 1518.0], [82.2, 1521.0], [82.3, 1528.0], [82.4, 1531.0], [82.5, 1533.0], [82.6, 1542.0], [82.7, 1547.0], [82.8, 1558.0], [82.9, 1559.0], [83.0, 1561.0], [83.1, 1562.0], [83.2, 1573.0], [83.3, 1577.0], [83.4, 1583.0], [83.5, 1588.0], [83.6, 1598.0], [83.7, 1603.0], [83.8, 1605.0], [83.9, 1607.0], [84.0, 1609.0], [84.1, 1611.0], [84.2, 1613.0], [84.3, 1616.0], [84.4, 1619.0], [84.5, 1626.0], [84.6, 1629.0], [84.7, 1629.0], [84.8, 1632.0], [84.9, 1637.0], [85.0, 1643.0], [85.1, 1650.0], [85.2, 1655.0], [85.3, 1663.0], [85.4, 1670.0], [85.5, 1675.0], [85.6, 1686.0], [85.7, 1689.0], [85.8, 1690.0], [85.9, 1692.0], [86.0, 1696.0], [86.1, 1700.0], [86.2, 1707.0], [86.3, 1709.0], [86.4, 1715.0], [86.5, 1719.0], [86.6, 1724.0], [86.7, 1736.0], [86.8, 1737.0], [86.9, 1742.0], [87.0, 1748.0], [87.1, 1749.0], [87.2, 1756.0], [87.3, 1762.0], [87.4, 1769.0], [87.5, 1770.0], [87.6, 1771.0], [87.7, 1778.0], [87.8, 1805.0], [87.9, 1811.0], [88.0, 1830.0], [88.1, 1836.0], [88.2, 1844.0], [88.3, 1855.0], [88.4, 1857.0], [88.5, 1858.0], [88.6, 1860.0], [88.7, 1864.0], [88.8, 1867.0], [88.9, 1877.0], [89.0, 1878.0], [89.1, 1881.0], [89.2, 1892.0], [89.3, 1894.0], [89.4, 1896.0], [89.5, 1896.0], [89.6, 1905.0], [89.7, 1906.0], [89.8, 1912.0], [89.9, 1914.0], [90.0, 1915.0], [90.1, 1923.0], [90.2, 1929.0], [90.3, 1942.0], [90.4, 1943.0], [90.5, 1950.0], [90.6, 1951.0], [90.7, 1951.0], [90.8, 1953.0], [90.9, 1956.0], [91.0, 1964.0], [91.1, 1967.0], [91.2, 1967.0], [91.3, 1985.0], [91.4, 1990.0], [91.5, 1994.0], [91.6, 2002.0], [91.7, 2005.0], [91.8, 2016.0], [91.9, 2025.0], [92.0, 2030.0], [92.1, 2043.0], [92.2, 2046.0], [92.3, 2052.0], [92.4, 2063.0], [92.5, 2072.0], [92.6, 2081.0], [92.7, 2084.0], [92.8, 2100.0], [92.9, 2102.0], [93.0, 2102.0], [93.1, 2103.0], [93.2, 2115.0], [93.3, 2120.0], [93.4, 2122.0], [93.5, 2134.0], [93.6, 2139.0], [93.7, 2149.0], [93.8, 2176.0], [93.9, 2189.0], [94.0, 2202.0], [94.1, 2217.0], [94.2, 2222.0], [94.3, 2239.0], [94.4, 2240.0], [94.5, 2261.0], [94.6, 2315.0], [94.7, 2339.0], [94.8, 2368.0], [94.9, 2396.0], [95.0, 2400.0], [95.1, 2406.0], [95.2, 2416.0], [95.3, 2418.0], [95.4, 2443.0], [95.5, 2452.0], [95.6, 2463.0], [95.7, 2482.0], [95.8, 2501.0], [95.9, 2504.0], [96.0, 2530.0], [96.1, 2532.0], [96.2, 2534.0], [96.3, 2535.0], [96.4, 2537.0], [96.5, 2553.0], [96.6, 2559.0], [96.7, 2562.0], [96.8, 2562.0], [96.9, 2563.0], [97.0, 2573.0], [97.1, 2580.0], [97.2, 2589.0], [97.3, 2601.0], [97.4, 2617.0], [97.5, 2618.0], [97.6, 2629.0], [97.7, 2638.0], [97.8, 2648.0], [97.9, 2652.0], [98.0, 2655.0], [98.1, 2656.0], [98.2, 2666.0], [98.3, 2673.0], [98.4, 2684.0], [98.5, 2687.0], [98.6, 2688.0], [98.7, 2695.0], [98.8, 2716.0], [98.9, 2721.0], [99.0, 2746.0], [99.1, 2751.0], [99.2, 2765.0], [99.3, 2768.0], [99.4, 2779.0], [99.5, 2790.0], [99.6, 2860.0], [99.7, 2912.0], [99.8, 2946.0], [99.9, 3209.0]], "isOverall": false, "label": "HTTP Request", "isController": false}], "supportsControllersDiscrimination": true, "maxX": 100.0, "title": "Response Time Percentiles"}},
        getOptions: function() {
            return {
                series: {
                    points: { show: false }
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendResponseTimePercentiles'
                },
                xaxis: {
                    tickDecimals: 1,
                    axisLabel: "Percentiles",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Percentile value in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s : %x.2 percentile was %y ms"
                },
                selection: { mode: "xy" },
            };
        },
        createGraph: function() {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesResponseTimePercentiles"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotResponseTimesPercentiles"), dataset, options);
            // setup overview
            $.plot($("#overviewResponseTimesPercentiles"), dataset, prepareOverviewOptions(options));
        }
};

/**
 * @param elementId Id of element where we display message
 */
function setEmptyGraph(elementId) {
    $(function() {
        $(elementId).text("No graph series with filter="+seriesFilter);
    });
}

// Response times percentiles
function refreshResponseTimePercentiles() {
    var infos = responseTimePercentilesInfos;
    prepareSeries(infos.data);
    if(infos.data.result.series.length == 0) {
        setEmptyGraph("#bodyResponseTimePercentiles");
        return;
    }
    if (isGraph($("#flotResponseTimesPercentiles"))){
        infos.createGraph();
    } else {
        var choiceContainer = $("#choicesResponseTimePercentiles");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotResponseTimesPercentiles", "#overviewResponseTimesPercentiles");
        $('#bodyResponseTimePercentiles .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
}

var responseTimeDistributionInfos = {
        data: {"result": {"minY": 1.0, "minX": 0.0, "maxY": 1492.0, "series": [{"data": [[0.0, 347.0], [600.0, 20.0], [700.0, 41.0], [800.0, 18.0], [900.0, 18.0], [1000.0, 28.0], [1100.0, 44.0], [1200.0, 77.0], [1300.0, 37.0], [1400.0, 45.0], [1500.0, 59.0], [100.0, 22.0], [1600.0, 74.0], [1700.0, 50.0], [1800.0, 55.0], [1900.0, 59.0], [2000.0, 37.0], [2100.0, 36.0], [2200.0, 18.0], [2300.0, 12.0], [2400.0, 24.0], [2500.0, 45.0], [2600.0, 44.0], [2700.0, 24.0], [2800.0, 3.0], [2900.0, 5.0], [3100.0, 2.0], [200.0, 1492.0], [3300.0, 1.0], [3200.0, 1.0], [3500.0, 1.0], [300.0, 201.0], [400.0, 44.0], [500.0, 16.0]], "isOverall": false, "label": "HTTP Request", "isController": false}], "supportsControllersDiscrimination": true, "granularity": 100, "maxX": 3500.0, "title": "Response Time Distribution"}},
        getOptions: function() {
            var granularity = this.data.result.granularity;
            return {
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendResponseTimeDistribution'
                },
                xaxis:{
                    axisLabel: "Response times in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Number of responses",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                bars : {
                    show: true,
                    barWidth: this.data.result.granularity
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: function(label, xval, yval, flotItem){
                        return yval + " responses for " + label + " were between " + xval + " and " + (xval + granularity) + " ms";
                    }
                }
            };
        },
        createGraph: function() {
            var data = this.data;
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotResponseTimeDistribution"), prepareData(data.result.series, $("#choicesResponseTimeDistribution")), options);
        }

};

// Response time distribution
function refreshResponseTimeDistribution() {
    var infos = responseTimeDistributionInfos;
    prepareSeries(infos.data);
    if(infos.data.result.series.length == 0) {
        setEmptyGraph("#bodyResponseTimeDistribution");
        return;
    }
    if (isGraph($("#flotResponseTimeDistribution"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesResponseTimeDistribution");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        $('#footerResponseTimeDistribution .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};


var syntheticResponseTimeDistributionInfos = {
        data: {"result": {"minY": 346.0, "minX": 0.0, "ticks": [[0, "Requests having \nresponse time <= 500ms"], [1, "Requests having \nresponse time > 500ms and <= 1,500ms"], [2, "Requests having \nresponse time > 1,500ms"], [3, "Requests in error"]], "maxY": 2106.0, "series": [{"data": [[0.0, 2106.0]], "color": "#9ACD32", "isOverall": false, "label": "Requests having \nresponse time <= 500ms", "isController": false}, {"data": [[1.0, 346.0]], "color": "yellow", "isOverall": false, "label": "Requests having \nresponse time > 500ms and <= 1,500ms", "isController": false}, {"data": [[2.0, 548.0]], "color": "orange", "isOverall": false, "label": "Requests having \nresponse time > 1,500ms", "isController": false}, {"data": [], "color": "#FF6347", "isOverall": false, "label": "Requests in error", "isController": false}], "supportsControllersDiscrimination": false, "maxX": 2.0, "title": "Synthetic Response Times Distribution"}},
        getOptions: function() {
            return {
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendSyntheticResponseTimeDistribution'
                },
                xaxis:{
                    axisLabel: "Response times ranges",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                    tickLength:0,
                    min:-0.5,
                    max:3.5
                },
                yaxis: {
                    axisLabel: "Number of responses",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                bars : {
                    show: true,
                    align: "center",
                    barWidth: 0.25,
                    fill:.75
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: function(label, xval, yval, flotItem){
                        return yval + " " + label;
                    }
                }
            };
        },
        createGraph: function() {
            var data = this.data;
            var options = this.getOptions();
            prepareOptions(options, data);
            options.xaxis.ticks = data.result.ticks;
            $.plot($("#flotSyntheticResponseTimeDistribution"), prepareData(data.result.series, $("#choicesSyntheticResponseTimeDistribution")), options);
        }

};

// Response time distribution
function refreshSyntheticResponseTimeDistribution() {
    var infos = syntheticResponseTimeDistributionInfos;
    prepareSeries(infos.data, true);
    if (isGraph($("#flotSyntheticResponseTimeDistribution"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesSyntheticResponseTimeDistribution");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        $('#footerSyntheticResponseTimeDistribution .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var activeThreadsOverTimeInfos = {
        data: {"result": {"minY": 13.582922013820344, "minX": 1.7791773E12, "maxY": 74.44250513347018, "series": [{"data": [[1.77917736E12, 74.44250513347018], [1.7791773E12, 13.582922013820344]], "isOverall": false, "label": "Thread Group", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 60000, "maxX": 1.77917736E12, "title": "Active Threads Over Time"}},
        getOptions: function() {
            return {
                series: {
                    stack: true,
                    lines: {
                        show: true,
                        fill: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Number of active threads",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20
                },
                legend: {
                    noColumns: 6,
                    show: true,
                    container: '#legendActiveThreadsOverTime'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                selection: {
                    mode: 'xy'
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s : At %x there were %y active threads"
                }
            };
        },
        createGraph: function() {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesActiveThreadsOverTime"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotActiveThreadsOverTime"), dataset, options);
            // setup overview
            $.plot($("#overviewActiveThreadsOverTime"), dataset, prepareOverviewOptions(options));
        }
};

// Active Threads Over Time
function refreshActiveThreadsOverTime(fixTimestamps) {
    var infos = activeThreadsOverTimeInfos;
    prepareSeries(infos.data);
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, 28800000);
    }
    if(isGraph($("#flotActiveThreadsOverTime"))) {
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesActiveThreadsOverTime");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotActiveThreadsOverTime", "#overviewActiveThreadsOverTime");
        $('#footerActiveThreadsOverTime .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var timeVsThreadsInfos = {
        data: {"result": {"minY": 57.0, "minX": 1.0, "maxY": 2764.0, "series": [{"data": [[2.0, 228.0], [3.0, 199.33333333333334], [4.0, 72.0], [5.0, 175.0], [6.0, 169.0], [7.0, 246.42857142857144], [8.0, 176.0], [9.0, 203.66666666666666], [10.0, 211.5], [11.0, 240.8], [12.0, 199.21900826446287], [13.0, 209.19929660023442], [14.0, 234.9882943143811], [15.0, 257.4295302013422], [16.0, 268.3934426229509], [17.0, 320.86046511627904], [18.0, 343.97222222222223], [19.0, 306.6923076923077], [20.0, 458.3], [21.0, 593.8333333333334], [23.0, 505.3333333333333], [25.0, 745.0], [26.0, 787.4], [29.0, 774.5], [30.0, 787.5714285714286], [31.0, 803.0], [33.0, 820.25], [35.0, 1325.421052631579], [34.0, 618.1428571428571], [36.0, 2019.5], [37.0, 1335.5], [41.0, 1550.125], [44.0, 2764.0], [45.0, 1906.0], [47.0, 1512.2307692307695], [48.0, 1299.0833333333333], [49.0, 723.0], [50.0, 1774.6923076923078], [51.0, 1581.5714285714284], [52.0, 998.5], [53.0, 805.0], [54.0, 424.5], [56.0, 1377.2222222222222], [57.0, 918.2142857142857], [59.0, 1150.2222222222222], [58.0, 1223.0], [60.0, 1281.5666666666668], [61.0, 839.8888888888889], [62.0, 1333.0], [63.0, 1475.1000000000001], [64.0, 995.9999999999999], [65.0, 1096.125], [66.0, 1313.8461538461536], [67.0, 1360.2380952380952], [68.0, 1209.923076923077], [69.0, 1302.3333333333335], [70.0, 1387.076923076923], [71.0, 925.9999999999999], [72.0, 1517.3749999999998], [73.0, 1368.689655172414], [75.0, 1298.5555555555557], [74.0, 1764.0], [76.0, 1804.6363636363637], [77.0, 1511.8], [79.0, 709.5], [78.0, 1855.0], [81.0, 1665.142857142857], [83.0, 1650.4062499999995], [82.0, 1962.0], [80.0, 1870.25], [85.0, 1682.5762711864402], [86.0, 1671.0769230769224], [84.0, 1272.0833333333333], [87.0, 1378.2], [91.0, 1834.372881355932], [88.0, 1961.0], [90.0, 2288.25], [89.0, 1890.5454545454547], [92.0, 927.8], [95.0, 2297.7272727272734], [94.0, 2218.4285714285716], [93.0, 2505.1], [96.0, 2501.8064516129043], [97.0, 1146.6666666666665], [98.0, 1288.0], [99.0, 1299.0], [101.0, 2469.5833333333335], [103.0, 1521.0], [102.0, 2639.5], [100.0, 2344.25], [104.0, 2302.714285714286], [107.0, 2467.333333333333], [106.0, 2500.5], [108.0, 1755.184210526316], [1.0, 57.0]], "isOverall": false, "label": "HTTP Request", "isController": false}, {"data": [[33.342000000000084, 663.8923333333323]], "isOverall": false, "label": "HTTP Request-Aggregated", "isController": false}], "supportsControllersDiscrimination": true, "maxX": 108.0, "title": "Time VS Threads"}},
        getOptions: function() {
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    axisLabel: "Number of active threads",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Average response times in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20
                },
                legend: { noColumns: 2,show: true, container: '#legendTimeVsThreads' },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s: At %x.2 active threads, Average response time was %y.2 ms"
                }
            };
        },
        createGraph: function() {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesTimeVsThreads"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotTimesVsThreads"), dataset, options);
            // setup overview
            $.plot($("#overviewTimesVsThreads"), dataset, prepareOverviewOptions(options));
        }
};

// Time vs threads
function refreshTimeVsThreads(){
    var infos = timeVsThreadsInfos;
    prepareSeries(infos.data);
    if(infos.data.result.series.length == 0) {
        setEmptyGraph("#bodyTimeVsThreads");
        return;
    }
    if(isGraph($("#flotTimesVsThreads"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesTimeVsThreads");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotTimesVsThreads", "#overviewTimesVsThreads");
        $('#footerTimeVsThreads .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var bytesThroughputOverTimeInfos = {
        data : {"result": {"minY": 6670.9, "minX": 1.7791773E12, "maxY": 14958.033333333333, "series": [{"data": [[1.77917736E12, 7075.3], [1.7791773E12, 14958.033333333333]], "isOverall": false, "label": "Bytes received per second", "isController": false}, {"data": [[1.77917736E12, 6670.9], [1.7791773E12, 13787.433333333332]], "isOverall": false, "label": "Bytes sent per second", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 60000, "maxX": 1.77917736E12, "title": "Bytes Throughput Over Time"}},
        getOptions : function(){
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity) ,
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Bytes / sec",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendBytesThroughputOverTime'
                },
                selection: {
                    mode: "xy"
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s at %x was %y"
                }
            };
        },
        createGraph : function() {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesBytesThroughputOverTime"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotBytesThroughputOverTime"), dataset, options);
            // setup overview
            $.plot($("#overviewBytesThroughputOverTime"), dataset, prepareOverviewOptions(options));
        }
};

// Bytes throughput Over Time
function refreshBytesThroughputOverTime(fixTimestamps) {
    var infos = bytesThroughputOverTimeInfos;
    prepareSeries(infos.data);
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, 28800000);
    }
    if(isGraph($("#flotBytesThroughputOverTime"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesBytesThroughputOverTime");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotBytesThroughputOverTime", "#overviewBytesThroughputOverTime");
        $('#footerBytesThroughputOverTime .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
}

var responseTimesOverTimeInfos = {
        data: {"result": {"minY": 226.56367226061226, "minX": 1.7791773E12, "maxY": 1573.5718685831605, "series": [{"data": [[1.77917736E12, 1573.5718685831605], [1.7791773E12, 226.56367226061226]], "isOverall": false, "label": "HTTP Request", "isController": false}], "supportsControllersDiscrimination": true, "granularity": 60000, "maxX": 1.77917736E12, "title": "Response Time Over Time"}},
        getOptions: function(){
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Average response time in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendResponseTimesOverTime'
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s : at %x Average response time was %y ms"
                }
            };
        },
        createGraph: function() {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesResponseTimesOverTime"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotResponseTimesOverTime"), dataset, options);
            // setup overview
            $.plot($("#overviewResponseTimesOverTime"), dataset, prepareOverviewOptions(options));
        }
};

// Response Times Over Time
function refreshResponseTimeOverTime(fixTimestamps) {
    var infos = responseTimesOverTimeInfos;
    prepareSeries(infos.data);
    if(infos.data.result.series.length == 0) {
        setEmptyGraph("#bodyResponseTimeOverTime");
        return;
    }
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, 28800000);
    }
    if(isGraph($("#flotResponseTimesOverTime"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesResponseTimesOverTime");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotResponseTimesOverTime", "#overviewResponseTimesOverTime");
        $('#footerResponseTimesOverTime .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var latenciesOverTimeInfos = {
        data: {"result": {"minY": 226.28282329713718, "minX": 1.7791773E12, "maxY": 1573.409650924024, "series": [{"data": [[1.77917736E12, 1573.409650924024], [1.7791773E12, 226.28282329713718]], "isOverall": false, "label": "HTTP Request", "isController": false}], "supportsControllersDiscrimination": true, "granularity": 60000, "maxX": 1.77917736E12, "title": "Latencies Over Time"}},
        getOptions: function() {
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Average response latencies in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendLatenciesOverTime'
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s : at %x Average latency was %y ms"
                }
            };
        },
        createGraph: function () {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesLatenciesOverTime"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotLatenciesOverTime"), dataset, options);
            // setup overview
            $.plot($("#overviewLatenciesOverTime"), dataset, prepareOverviewOptions(options));
        }
};

// Latencies Over Time
function refreshLatenciesOverTime(fixTimestamps) {
    var infos = latenciesOverTimeInfos;
    prepareSeries(infos.data);
    if(infos.data.result.series.length == 0) {
        setEmptyGraph("#bodyLatenciesOverTime");
        return;
    }
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, 28800000);
    }
    if(isGraph($("#flotLatenciesOverTime"))) {
        infos.createGraph();
    }else {
        var choiceContainer = $("#choicesLatenciesOverTime");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotLatenciesOverTime", "#overviewLatenciesOverTime");
        $('#footerLatenciesOverTime .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var connectTimeOverTimeInfos = {
        data: {"result": {"minY": 0.13622902270483725, "minX": 1.7791773E12, "maxY": 0.15503080082135542, "series": [{"data": [[1.77917736E12, 0.15503080082135542], [1.7791773E12, 0.13622902270483725]], "isOverall": false, "label": "HTTP Request", "isController": false}], "supportsControllersDiscrimination": true, "granularity": 60000, "maxX": 1.77917736E12, "title": "Connect Time Over Time"}},
        getOptions: function() {
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getConnectTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Average Connect Time in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendConnectTimeOverTime'
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s : at %x Average connect time was %y ms"
                }
            };
        },
        createGraph: function () {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesConnectTimeOverTime"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotConnectTimeOverTime"), dataset, options);
            // setup overview
            $.plot($("#overviewConnectTimeOverTime"), dataset, prepareOverviewOptions(options));
        }
};

// Connect Time Over Time
function refreshConnectTimeOverTime(fixTimestamps) {
    var infos = connectTimeOverTimeInfos;
    prepareSeries(infos.data);
    if(infos.data.result.series.length == 0) {
        setEmptyGraph("#bodyConnectTimeOverTime");
        return;
    }
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, 28800000);
    }
    if(isGraph($("#flotConnectTimeOverTime"))) {
        infos.createGraph();
    }else {
        var choiceContainer = $("#choicesConnectTimeOverTime");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotConnectTimeOverTime", "#overviewConnectTimeOverTime");
        $('#footerConnectTimeOverTime .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var responseTimePercentilesOverTimeInfos = {
        data: {"result": {"minY": 44.0, "minX": 1.7791773E12, "maxY": 3586.0, "series": [{"data": [[1.77917736E12, 3586.0], [1.7791773E12, 559.0]], "isOverall": false, "label": "Max", "isController": false}, {"data": [[1.77917736E12, 2562.0], [1.7791773E12, 300.0]], "isOverall": false, "label": "90th percentile", "isController": false}, {"data": [[1.77917736E12, 2905.25], [1.7791773E12, 430.84000000000015]], "isOverall": false, "label": "99th percentile", "isController": false}, {"data": [[1.77917736E12, 2683.25], [1.7791773E12, 335.0]], "isOverall": false, "label": "95th percentile", "isController": false}, {"data": [[1.77917736E12, 64.0], [1.7791773E12, 44.0]], "isOverall": false, "label": "Min", "isController": false}, {"data": [[1.77917736E12, 1603.5], [1.7791773E12, 243.0]], "isOverall": false, "label": "Median", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 60000, "maxX": 1.77917736E12, "title": "Response Time Percentiles Over Time (successful requests only)"}},
        getOptions: function() {
            return {
                series: {
                    lines: {
                        show: true,
                        fill: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Response Time in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendResponseTimePercentilesOverTime'
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s : at %x Response time was %y ms"
                }
            };
        },
        createGraph: function () {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesResponseTimePercentilesOverTime"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotResponseTimePercentilesOverTime"), dataset, options);
            // setup overview
            $.plot($("#overviewResponseTimePercentilesOverTime"), dataset, prepareOverviewOptions(options));
        }
};

// Response Time Percentiles Over Time
function refreshResponseTimePercentilesOverTime(fixTimestamps) {
    var infos = responseTimePercentilesOverTimeInfos;
    prepareSeries(infos.data);
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, 28800000);
    }
    if(isGraph($("#flotResponseTimePercentilesOverTime"))) {
        infos.createGraph();
    }else {
        var choiceContainer = $("#choicesResponseTimePercentilesOverTime");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotResponseTimePercentilesOverTime", "#overviewResponseTimePercentilesOverTime");
        $('#footerResponseTimePercentilesOverTime .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};


var responseTimeVsRequestInfos = {
    data: {"result": {"minY": 226.5, "minX": 2.0, "maxY": 2617.0, "series": [{"data": [[33.0, 1236.0], [2.0, 1948.0], [34.0, 1590.5], [43.0, 1689.0], [46.0, 406.5], [3.0, 769.5], [51.0, 309.5], [53.0, 273.0], [55.0, 269.0], [54.0, 379.0], [57.0, 286.5], [58.0, 241.5], [59.0, 236.5], [60.0, 241.0], [61.0, 245.0], [63.0, 248.0], [62.0, 243.0], [64.0, 236.0], [66.0, 235.5], [87.0, 1914.0], [95.0, 2617.0], [94.0, 2482.5], [93.0, 1906.0], [96.0, 1629.0], [7.0, 754.0], [8.0, 1549.0], [10.0, 2611.0], [15.0, 1302.0], [17.0, 773.0], [22.0, 226.5]], "isOverall": false, "label": "Successes", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 1000, "maxX": 96.0, "title": "Response Time Vs Request"}},
    getOptions: function() {
        return {
            series: {
                lines: {
                    show: false
                },
                points: {
                    show: true
                }
            },
            xaxis: {
                axisLabel: "Global number of requests per second",
                axisLabelUseCanvas: true,
                axisLabelFontSizePixels: 12,
                axisLabelFontFamily: 'Verdana, Arial',
                axisLabelPadding: 20,
            },
            yaxis: {
                axisLabel: "Median Response Time in ms",
                axisLabelUseCanvas: true,
                axisLabelFontSizePixels: 12,
                axisLabelFontFamily: 'Verdana, Arial',
                axisLabelPadding: 20,
            },
            legend: {
                noColumns: 2,
                show: true,
                container: '#legendResponseTimeVsRequest'
            },
            selection: {
                mode: 'xy'
            },
            grid: {
                hoverable: true // IMPORTANT! this is needed for tooltip to work
            },
            tooltip: true,
            tooltipOpts: {
                content: "%s : Median response time at %x req/s was %y ms"
            },
            colors: ["#9ACD32", "#FF6347"]
        };
    },
    createGraph: function () {
        var data = this.data;
        var dataset = prepareData(data.result.series, $("#choicesResponseTimeVsRequest"));
        var options = this.getOptions();
        prepareOptions(options, data);
        $.plot($("#flotResponseTimeVsRequest"), dataset, options);
        // setup overview
        $.plot($("#overviewResponseTimeVsRequest"), dataset, prepareOverviewOptions(options));

    }
};

// Response Time vs Request
function refreshResponseTimeVsRequest() {
    var infos = responseTimeVsRequestInfos;
    prepareSeries(infos.data);
    if (isGraph($("#flotResponseTimeVsRequest"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesResponseTimeVsRequest");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotResponseTimeVsRequest", "#overviewResponseTimeVsRequest");
        $('#footerResponseRimeVsRequest .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};


var latenciesVsRequestInfos = {
    data: {"result": {"minY": 225.5, "minX": 2.0, "maxY": 2617.0, "series": [{"data": [[33.0, 1236.0], [2.0, 1948.0], [34.0, 1590.5], [43.0, 1689.0], [46.0, 406.5], [3.0, 769.5], [51.0, 309.5], [53.0, 272.0], [55.0, 268.0], [54.0, 378.5], [57.0, 286.5], [58.0, 241.5], [59.0, 236.0], [60.0, 241.0], [61.0, 245.0], [63.0, 248.0], [62.0, 243.0], [64.0, 236.0], [66.0, 235.0], [87.0, 1914.0], [95.0, 2617.0], [94.0, 2482.5], [93.0, 1906.0], [96.0, 1629.0], [7.0, 752.0], [8.0, 1549.0], [10.0, 2611.0], [15.0, 1302.0], [17.0, 773.0], [22.0, 225.5]], "isOverall": false, "label": "Successes", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 1000, "maxX": 96.0, "title": "Latencies Vs Request"}},
    getOptions: function() {
        return{
            series: {
                lines: {
                    show: false
                },
                points: {
                    show: true
                }
            },
            xaxis: {
                axisLabel: "Global number of requests per second",
                axisLabelUseCanvas: true,
                axisLabelFontSizePixels: 12,
                axisLabelFontFamily: 'Verdana, Arial',
                axisLabelPadding: 20,
            },
            yaxis: {
                axisLabel: "Median Latency in ms",
                axisLabelUseCanvas: true,
                axisLabelFontSizePixels: 12,
                axisLabelFontFamily: 'Verdana, Arial',
                axisLabelPadding: 20,
            },
            legend: { noColumns: 2,show: true, container: '#legendLatencyVsRequest' },
            selection: {
                mode: 'xy'
            },
            grid: {
                hoverable: true // IMPORTANT! this is needed for tooltip to work
            },
            tooltip: true,
            tooltipOpts: {
                content: "%s : Median Latency time at %x req/s was %y ms"
            },
            colors: ["#9ACD32", "#FF6347"]
        };
    },
    createGraph: function () {
        var data = this.data;
        var dataset = prepareData(data.result.series, $("#choicesLatencyVsRequest"));
        var options = this.getOptions();
        prepareOptions(options, data);
        $.plot($("#flotLatenciesVsRequest"), dataset, options);
        // setup overview
        $.plot($("#overviewLatenciesVsRequest"), dataset, prepareOverviewOptions(options));
    }
};

// Latencies vs Request
function refreshLatenciesVsRequest() {
        var infos = latenciesVsRequestInfos;
        prepareSeries(infos.data);
        if(isGraph($("#flotLatenciesVsRequest"))){
            infos.createGraph();
        }else{
            var choiceContainer = $("#choicesLatencyVsRequest");
            createLegend(choiceContainer, infos);
            infos.createGraph();
            setGraphZoomable("#flotLatenciesVsRequest", "#overviewLatenciesVsRequest");
            $('#footerLatenciesVsRequest .legendColorBox > div').each(function(i){
                $(this).clone().prependTo(choiceContainer.find("li").eq(i));
            });
        }
};

var hitsPerSecondInfos = {
        data: {"result": {"minY": 15.866666666666667, "minX": 1.7791773E12, "maxY": 34.13333333333333, "series": [{"data": [[1.77917736E12, 15.866666666666667], [1.7791773E12, 34.13333333333333]], "isOverall": false, "label": "hitsPerSecond", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 60000, "maxX": 1.77917736E12, "title": "Hits Per Second"}},
        getOptions: function() {
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Number of hits / sec",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: "#legendHitsPerSecond"
                },
                selection: {
                    mode : 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s at %x was %y.2 hits/sec"
                }
            };
        },
        createGraph: function createGraph() {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesHitsPerSecond"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotHitsPerSecond"), dataset, options);
            // setup overview
            $.plot($("#overviewHitsPerSecond"), dataset, prepareOverviewOptions(options));
        }
};

// Hits per second
function refreshHitsPerSecond(fixTimestamps) {
    var infos = hitsPerSecondInfos;
    prepareSeries(infos.data);
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, 28800000);
    }
    if (isGraph($("#flotHitsPerSecond"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesHitsPerSecond");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotHitsPerSecond", "#overviewHitsPerSecond");
        $('#footerHitsPerSecond .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
}

var codesPerSecondInfos = {
        data: {"result": {"minY": 16.233333333333334, "minX": 1.7791773E12, "maxY": 33.766666666666666, "series": [{"data": [[1.77917736E12, 16.233333333333334], [1.7791773E12, 33.766666666666666]], "isOverall": false, "label": "200", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 60000, "maxX": 1.77917736E12, "title": "Codes Per Second"}},
        getOptions: function(){
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Number of responses / sec",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: "#legendCodesPerSecond"
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "Number of Response Codes %s at %x was %y.2 responses / sec"
                }
            };
        },
    createGraph: function() {
        var data = this.data;
        var dataset = prepareData(data.result.series, $("#choicesCodesPerSecond"));
        var options = this.getOptions();
        prepareOptions(options, data);
        $.plot($("#flotCodesPerSecond"), dataset, options);
        // setup overview
        $.plot($("#overviewCodesPerSecond"), dataset, prepareOverviewOptions(options));
    }
};

// Codes per second
function refreshCodesPerSecond(fixTimestamps) {
    var infos = codesPerSecondInfos;
    prepareSeries(infos.data);
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, 28800000);
    }
    if(isGraph($("#flotCodesPerSecond"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesCodesPerSecond");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotCodesPerSecond", "#overviewCodesPerSecond");
        $('#footerCodesPerSecond .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var transactionsPerSecondInfos = {
        data: {"result": {"minY": 16.233333333333334, "minX": 1.7791773E12, "maxY": 33.766666666666666, "series": [{"data": [[1.77917736E12, 16.233333333333334], [1.7791773E12, 33.766666666666666]], "isOverall": false, "label": "HTTP Request-success", "isController": false}], "supportsControllersDiscrimination": true, "granularity": 60000, "maxX": 1.77917736E12, "title": "Transactions Per Second"}},
        getOptions: function(){
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Number of transactions / sec",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: "#legendTransactionsPerSecond"
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s at %x was %y transactions / sec"
                }
            };
        },
    createGraph: function () {
        var data = this.data;
        var dataset = prepareData(data.result.series, $("#choicesTransactionsPerSecond"));
        var options = this.getOptions();
        prepareOptions(options, data);
        $.plot($("#flotTransactionsPerSecond"), dataset, options);
        // setup overview
        $.plot($("#overviewTransactionsPerSecond"), dataset, prepareOverviewOptions(options));
    }
};

// Transactions per second
function refreshTransactionsPerSecond(fixTimestamps) {
    var infos = transactionsPerSecondInfos;
    prepareSeries(infos.data);
    if(infos.data.result.series.length == 0) {
        setEmptyGraph("#bodyTransactionsPerSecond");
        return;
    }
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, 28800000);
    }
    if(isGraph($("#flotTransactionsPerSecond"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesTransactionsPerSecond");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotTransactionsPerSecond", "#overviewTransactionsPerSecond");
        $('#footerTransactionsPerSecond .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var totalTPSInfos = {
        data: {"result": {"minY": 16.233333333333334, "minX": 1.7791773E12, "maxY": 33.766666666666666, "series": [{"data": [[1.77917736E12, 16.233333333333334], [1.7791773E12, 33.766666666666666]], "isOverall": false, "label": "Transaction-success", "isController": false}, {"data": [], "isOverall": false, "label": "Transaction-failure", "isController": false}], "supportsControllersDiscrimination": true, "granularity": 60000, "maxX": 1.77917736E12, "title": "Total Transactions Per Second"}},
        getOptions: function(){
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Number of transactions / sec",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: "#legendTotalTPS"
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s at %x was %y transactions / sec"
                },
                colors: ["#9ACD32", "#FF6347"]
            };
        },
    createGraph: function () {
        var data = this.data;
        var dataset = prepareData(data.result.series, $("#choicesTotalTPS"));
        var options = this.getOptions();
        prepareOptions(options, data);
        $.plot($("#flotTotalTPS"), dataset, options);
        // setup overview
        $.plot($("#overviewTotalTPS"), dataset, prepareOverviewOptions(options));
    }
};

// Total Transactions per second
function refreshTotalTPS(fixTimestamps) {
    var infos = totalTPSInfos;
    // We want to ignore seriesFilter
    prepareSeries(infos.data, false, true);
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, 28800000);
    }
    if(isGraph($("#flotTotalTPS"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesTotalTPS");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotTotalTPS", "#overviewTotalTPS");
        $('#footerTotalTPS .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

// Collapse the graph matching the specified DOM element depending the collapsed
// status
function collapse(elem, collapsed){
    if(collapsed){
        $(elem).parent().find(".fa-chevron-up").removeClass("fa-chevron-up").addClass("fa-chevron-down");
    } else {
        $(elem).parent().find(".fa-chevron-down").removeClass("fa-chevron-down").addClass("fa-chevron-up");
        if (elem.id == "bodyBytesThroughputOverTime") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshBytesThroughputOverTime(true);
            }
            document.location.href="#bytesThroughputOverTime";
        } else if (elem.id == "bodyLatenciesOverTime") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshLatenciesOverTime(true);
            }
            document.location.href="#latenciesOverTime";
        } else if (elem.id == "bodyCustomGraph") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshCustomGraph(true);
            }
            document.location.href="#responseCustomGraph";
        } else if (elem.id == "bodyConnectTimeOverTime") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshConnectTimeOverTime(true);
            }
            document.location.href="#connectTimeOverTime";
        } else if (elem.id == "bodyResponseTimePercentilesOverTime") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshResponseTimePercentilesOverTime(true);
            }
            document.location.href="#responseTimePercentilesOverTime";
        } else if (elem.id == "bodyResponseTimeDistribution") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshResponseTimeDistribution();
            }
            document.location.href="#responseTimeDistribution" ;
        } else if (elem.id == "bodySyntheticResponseTimeDistribution") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshSyntheticResponseTimeDistribution();
            }
            document.location.href="#syntheticResponseTimeDistribution" ;
        } else if (elem.id == "bodyActiveThreadsOverTime") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshActiveThreadsOverTime(true);
            }
            document.location.href="#activeThreadsOverTime";
        } else if (elem.id == "bodyTimeVsThreads") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshTimeVsThreads();
            }
            document.location.href="#timeVsThreads" ;
        } else if (elem.id == "bodyCodesPerSecond") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshCodesPerSecond(true);
            }
            document.location.href="#codesPerSecond";
        } else if (elem.id == "bodyTransactionsPerSecond") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshTransactionsPerSecond(true);
            }
            document.location.href="#transactionsPerSecond";
        } else if (elem.id == "bodyTotalTPS") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshTotalTPS(true);
            }
            document.location.href="#totalTPS";
        } else if (elem.id == "bodyResponseTimeVsRequest") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshResponseTimeVsRequest();
            }
            document.location.href="#responseTimeVsRequest";
        } else if (elem.id == "bodyLatenciesVsRequest") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshLatenciesVsRequest();
            }
            document.location.href="#latencyVsRequest";
        }
    }
}

/*
 * Activates or deactivates all series of the specified graph (represented by id parameter)
 * depending on checked argument.
 */
function toggleAll(id, checked){
    var placeholder = document.getElementById(id);

    var cases = $(placeholder).find(':checkbox');
    cases.prop('checked', checked);
    $(cases).parent().children().children().toggleClass("legend-disabled", !checked);

    var choiceContainer;
    if ( id == "choicesBytesThroughputOverTime"){
        choiceContainer = $("#choicesBytesThroughputOverTime");
        refreshBytesThroughputOverTime(false);
    } else if(id == "choicesResponseTimesOverTime"){
        choiceContainer = $("#choicesResponseTimesOverTime");
        refreshResponseTimeOverTime(false);
    }else if(id == "choicesResponseCustomGraph"){
        choiceContainer = $("#choicesResponseCustomGraph");
        refreshCustomGraph(false);
    } else if ( id == "choicesLatenciesOverTime"){
        choiceContainer = $("#choicesLatenciesOverTime");
        refreshLatenciesOverTime(false);
    } else if ( id == "choicesConnectTimeOverTime"){
        choiceContainer = $("#choicesConnectTimeOverTime");
        refreshConnectTimeOverTime(false);
    } else if ( id == "choicesResponseTimePercentilesOverTime"){
        choiceContainer = $("#choicesResponseTimePercentilesOverTime");
        refreshResponseTimePercentilesOverTime(false);
    } else if ( id == "choicesResponseTimePercentiles"){
        choiceContainer = $("#choicesResponseTimePercentiles");
        refreshResponseTimePercentiles();
    } else if(id == "choicesActiveThreadsOverTime"){
        choiceContainer = $("#choicesActiveThreadsOverTime");
        refreshActiveThreadsOverTime(false);
    } else if ( id == "choicesTimeVsThreads"){
        choiceContainer = $("#choicesTimeVsThreads");
        refreshTimeVsThreads();
    } else if ( id == "choicesSyntheticResponseTimeDistribution"){
        choiceContainer = $("#choicesSyntheticResponseTimeDistribution");
        refreshSyntheticResponseTimeDistribution();
    } else if ( id == "choicesResponseTimeDistribution"){
        choiceContainer = $("#choicesResponseTimeDistribution");
        refreshResponseTimeDistribution();
    } else if ( id == "choicesHitsPerSecond"){
        choiceContainer = $("#choicesHitsPerSecond");
        refreshHitsPerSecond(false);
    } else if(id == "choicesCodesPerSecond"){
        choiceContainer = $("#choicesCodesPerSecond");
        refreshCodesPerSecond(false);
    } else if ( id == "choicesTransactionsPerSecond"){
        choiceContainer = $("#choicesTransactionsPerSecond");
        refreshTransactionsPerSecond(false);
    } else if ( id == "choicesTotalTPS"){
        choiceContainer = $("#choicesTotalTPS");
        refreshTotalTPS(false);
    } else if ( id == "choicesResponseTimeVsRequest"){
        choiceContainer = $("#choicesResponseTimeVsRequest");
        refreshResponseTimeVsRequest();
    } else if ( id == "choicesLatencyVsRequest"){
        choiceContainer = $("#choicesLatencyVsRequest");
        refreshLatenciesVsRequest();
    }
    var color = checked ? "black" : "#818181";
    if(choiceContainer != null) {
        choiceContainer.find("label").each(function(){
            this.style.color = color;
        });
    }
}

