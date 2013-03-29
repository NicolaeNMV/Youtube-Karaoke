var lyricsMap = [];
console.log("ZKaraoké! 0.1 Loaded");
(function($) {
    var subtitleOffSet = -0.8;

    var videoElement = $("#movie_player") || $('video')
    //.getCurrentTime()
    /**
    {
        "index": 0,
        "start":1,
        "end":0
    }
     */
    $('body').append( $("<style>\
        .zktime { margin-left:5px; float: right; \
        #ytl-outerwrapper { width:400px !important; background: white; }\
        .zkline { cursor: pointer; }\
        #track { font-size: 15px; position:absolute; bottom:10px; left:150px; color:white; background-color: rgba(0,0,0,0.4) } \
        .track-full-screen { font-size: 30px; bottom: 40px; left: 450px } \
    }</style>") );
    var display = $('<div id="track"></div>').appendTo( videoElement.parent() )


    $('#ytl-lyrics').contents().filter(function() {
        return this.nodeType == 3; // TEXT NODE
    }).each(function(index, line) {
        var newLine = $('<span class="zkline"><span class="zklyric"></span><span class="zktime"></span></span>');
        newLine.find(".zklyric").text( line.data );
        $(line).replaceWith( newLine );
    })
    $('#ytl-lyrics .zkline').each(function(index, line) {
        $(line).data("index", index);
    })

    function putLyric(index, lyric, start, end) {
        start = Math.ceil(start * 1000) / 1000; // round to 3 decimals
        lyricsMap[index] = {
            index: index,
            start: start,
            end: end,
            lyric: lyric
        }
        persistent.save();
    }

    function getLyric(second) {
        for (i = 0; i < lyricsMap.length; i++) {
            if (lyricsMap[i] && lyricsMap[i].start >= second) {
                if (i == 0) i = 1;
                return lyricsMap[i-1];
            }
        }
    }

    function getVideoId() {
        return /v=([\w-]{11})/.exec(window.location.search)[1]
    }

    var persistent = {
        prefix: function() {
            //return "zk" + window.location.pathname + window.location.search;
            return "zk" + getVideoId()
        }
        save: function() {
            localStorage[prefix()] = JSON.stringify(lyricsMap);
        },
        load: function() {
            var sval = localStorage[prefix()];
            if (sval) {
                lyricsMap = []
                JSON.parse(sval).forEach(function(val) {
                    if(val) {
                        lyricsMap[val.index] = val;
                    }
                } )
            }
        }
    } 

    function formatTime(totalSeconds) {
        var totalSeconds = Math.floor(totalSeconds);
        var minutes = Math.floor(totalSeconds / 60);
        var seconds = totalSeconds - minutes * 60;
        return minutes + ":" + (seconds < 10 ? "0" + seconds : seconds);
    }

    function getCurrentTime() {
        if (videoElement.is("video")) {
            return videoElement[0].currentTime;
        } else
            return videoElement[0].getCurrentTime();
    }

    $('body').on('click','#ytl-lyrics .zkline',function(e) {
        var seconds = getCurrentTime();
        $(".zktime",this).text( formatTime( Math.floor( seconds ) ) );
        putLyric( $(this).data("index"), $(".zklyric",this).text(), seconds );
    })

    var current = {index: null};

    function onTimeUpdate() {
            var newLyric = getLyric(this.currentTime - subtitleOffSet);
            console.log("newLyric ", newLyric);
            if (newLyric && current.index != newLyric.index) {
                current.index = newLyric.index;
                setCurrentLyric(newLyric.lyric);
            }
        }

    if (videoElement.is("video")) {
        $("video").bind("timeupdate", onTimeUpdate)
    } else {
        setInterval(onTimeUpdate, 200);
    }

    function setCurrentLyric(text) {
        display.text(text);
    }

    var isFullScreen = false;
    $(document).bind('webkitfullscreenchange mozfullscreenchange fullscreenchange',
        function(e){
            isFullScreen = !isFullScreen;
            display.toggleClass("track-full-screen",isFullScreen);
        }
    );

    function loadLineTime() {
        $(".zkline").each(function(index, line) {
            var index = $(line).data("index");
            if (lyricsMap[index]) {
                var seconds = Math.floor(lyricsMap[index].start);
                $(".zktime",line).text( formatTime( seconds ) );
            }
        });
    }

    function run() {
        persistent.load();
        loadLineTime();
    }
    run();
})(jQuery);