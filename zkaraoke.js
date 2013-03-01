var lyricsMap = [];
console.log("ZKaraoké! 0.1 Loaded");
(function($) {
    var subtitleOffSet = -0.8;
    /**
    {
        "index": 0,
        "start":1,
        "end":0
    }
     */
    $('body').append( $("<style>.zktime{ margin-left:5px; float: right; }</style>") );
    $('body').append( $("<style>#ytl-outerwrapper{ width:400px !important; background: white; }</style>") );
    $('body').append( $("<style>.zkline{ cursor: pointer; }</style>") );
    var display = $('<div id="track" style="position:absolute; bottom:10px; left:200px;color:white; background-color: rgba(0,0,0,0.4)"></div>').appendTo( $("video").parent() )

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
/**
 * 1 
 * 3
 * 6
 */
    function getLyric(second) {
        for (i = 0; i < lyricsMap.length; i++) {
            if (lyricsMap[i] && lyricsMap[i].start >= second) {
                if (i == 0) i = 1;
                return lyricsMap[i-1];
            }
        }
    }

    var persistent = {
        save: function() {
            localStorage["zk" + window.location.pathname] = JSON.stringify(lyricsMap);
        },
        load: function() {
            //console.log("zk" + window.location.pathname)
            var sval = localStorage["zk" + window.location.pathname];
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

    $('body').on('click','#ytl-lyrics .zkline',function(e) {
        var seconds = $("video")[0].currentTime;
        $(".zktime",this).text( formatTime( Math.floor( seconds ) ) );
        putLyric( $(this).data("index"), $(".zklyric",this).text(), seconds );
    })

    var current = {index: null};
    $("video").bind("timeupdate",
        function() {
            var newLyric = getLyric(this.currentTime - subtitleOffSet);
            //console.log("newLyric ", newLyric);
            if (newLyric && current.index != newLyric.index) {
                current.index = newLyric.index;
                setCurrentLyric(newLyric.lyric);
            }
        }
    )

    function setCurrentLyric(text) {
        display.text(text);
    }

    var isFullScreen = false;
    $(document).bind('webkitfullscreenchange mozfullscreenchange fullscreenchange',
        function(e){
            isFullScreen = !isFullScreen;
            console.log("Now: ",isFullScreen);
            if (isFullScreen) {
                display.css("font-size","30px");
                display.css("bottom","40px");
                display.css("left","450px");
            } else {
                display.css("font-size","15px");
                display.css("bottom","10px");
                display.css("left","200px");
            }
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