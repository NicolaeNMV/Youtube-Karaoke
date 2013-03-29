(function(){
    function load_zkaraoke() {
        console.log("load_zkaraoke");
        $('body').append( $("<script></script>").attr("src", "http://127.0.0.1:8000/zkaraoke.js" ) );
    }
    if(!document.getElementById('jQscript')) {
        var jQscript=document.createElement('script');
        jQscript.id='jQscript';
        jQscript.src='//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js';
        jQscript.onload = load_zkaraoke;
        document.documentElement.appendChild(jQscript);
    } else load_zkaraoke();
})();