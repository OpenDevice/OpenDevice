/**
 * @fileOverview 
 * Defines UnityObject2
 */


//TODO: No need to polute the global space, just transfer this control to a 'static' variable insite unityObject! 
/**
 * @namespace 
 */
//var unity = unity || {};
// We store all unityObject instances in a global scope, needed for IE firstFrameCallback and other internal tasks.
//unity.instances = [];
//unity.instanceNumber = 0;

/**
 * Object expected by the Java Installer. We can move those to UnityObject2 if we update the java Installer.
 */
var unityObject = {
    /**
     * Callback used bt the Java installer to notify the Install Complete.
     * @private
     * @param {String} id
     * @param {bool} success
     * @param {String} errormessage
     */
    javaInstallDone : function (id, success, errormessage) {

        var instanceId = parseInt(id.substring(id.lastIndexOf('_') + 1), 10);

        if (!isNaN(instanceId)) {

            // javaInstallDoneCallback must not be called directly because it deadlocks google chrome
            setTimeout(function () {

                UnityObject2.instances[instanceId].javaInstallDoneCallback(id, success, errormessage);
            }, 10);
        }
    }
};


/** 
 *  @class 
 *  @constructor
 */
var UnityObject2 = function (config) {

    /** @private */
    var logHistory = [],
        win = window,
        doc = document,
        nav = navigator,
        instanceNumber = null,
        //domLoaded = false,
        //domLoadEvents = [],
        embeddedObjects = [], //Could be removed?
        //listeners = [],
        //styleSheet = null,
        //styleSheetMedia = null,
        //autoHideShow = true,
        //fullSizeMissing = true,
        useSSL = (document.location.protocol == 'https:'),  //This will turn off enableUnityAnalytics, since enableUnityAnalytics don't have a https version.
        baseDomain = useSSL ? "https://ssl-webplayer.unity3d.com/" : "http://webplayer.unity3d.com/",
        triedJavaCookie = "_unity_triedjava",
        triedJavaInstall = _getCookie(triedJavaCookie),
        triedClickOnceCookie = "_unity_triedclickonce",
        triedClickOnce = _getCookie(triedClickOnceCookie),
        progressCallback = false,
        applets = [],
        //addedClickOnce = false,
        googleAnalyticsLoaded = false,
        googleAnalyticsCallback = null,
        latestStatus = null,
        lastType = null,
        //beginCallback = [],
        //preCallback = [],
        imagesToWaitFor = [],
        //referrer = null,
        pluginStatus = null,
        pluginStatusHistory = [],
        installProcessStarted = false, //not used anymore?
        kInstalled = "installed",
        kMissing = "missing",
        kBroken = "broken",
        kUnsupported = "unsupported",
        kReady = "ready", //not used anymore?
        kStart = "start",
        kError = "error",
        kFirst = "first",
        //kStandard = "standard",
        kJava = "java",
        kClickOnce = "clickonce", //not used anymore?
        wasMissing = false,             //identifies if this is a install attempt, or if the plugin was already installed
		unityObject = null,				//The <embed> or <object> for the webplayer. This can be used for webPlayer communication.
        //kApplet = "_applet",
        //kBanner = "_banner",

        cfg = {
            pluginName              : "Unity Player",
            pluginMimeType          : "application/vnd.unity",
            baseDownloadUrl         : baseDomain + "download_webplayer-3.x/",
            fullInstall             : false,
            autoInstall             : false,
            enableJava              : true,
            enableJVMPreloading     : false,
            enableClickOnce         : true,
            enableUnityAnalytics    : false,
            enableGoogleAnalytics   : true,
            params                  : {},
            attributes              : {},
            referrer                : null,
            debugLevel              : 0
        };

    // Merge in the given configuration and override defaults.
    cfg = jQuery.extend(true, cfg, config);

    if (cfg.referrer === "") {
        cfg.referrer = null;
    }
    //enableUnityAnalytics does not support SSL yet.
    if (useSSL) {
        cfg.enableUnityAnalytics = false;
    }

    /** 
     * Get cookie value
     * @private
     * @param {String} name The param name
     * @return string or false if non-existing.
     */
    function _getCookie(name) {

        var e = new RegExp(escape(name) + "=([^;]+)");

        if (e.test(doc.cookie + ";")) {

            e.exec(doc.cookie + ";");
            return RegExp.$1;
        }

        return false;
    }

    /** 
     * Sets session cookie
     * @private
     */
    function _setSessionCookie(name, value) {
        
        document.cookie = escape(name) + "=" + escape(value) + "; path=/";
    }

    /**
     * Converts unity version to number (used for version comparison)
     * @private
     */
    function _getNumericUnityVersion(version) {

        var result = 0,
            major,
            minor,
            fix,
            type,
            release;

        if (version) {

            var m = version.toLowerCase().match(/^(\d+)(?:\.(\d+)(?:\.(\d+)([dabfr])?(\d+)?)?)?$/);

            if (m && m[1]) {

                major = m[1];
                minor = m[2] ? m[2] : 0;
                fix = m[3] ? m[3] : 0;
                type = m[4] ? m[4] : 'r';
                release = m[5] ? m[5] : 0;
                result |= ((major / 10) % 10) << 28;
                result |= (major % 10) << 24;
                result |= (minor % 10) << 20;
                result |= (fix % 10) << 16;
                result |= {d: 2 << 12, a: 4 << 12, b: 6 << 12, f: 8 << 12, r: 8 << 12}[type];
                result |= ((release / 100) % 10) << 8;
                result |= ((release / 10) % 10) << 4;
                result |= (release % 10);
            }
        }
        
        return result;
    }

    /**
     * Gets plugin and unity versions (non-ie)
     * @private
     */
    function _getPluginVersion(callback, versions) {
        
        var b = doc.getElementsByTagName("body")[0];
        var ue = doc.createElement("object");
        var i = 0;
        
        if (b && ue) {
            ue.setAttribute("type", cfg.pluginMimeType);
            ue.style.visibility = "hidden";
            b.appendChild(ue);
            var count = 0;
            
            (function () {
                if (typeof ue.GetPluginVersion === "undefined") {
                    
                    if (count++ < 10) {
                        
                        setTimeout(arguments.callee, 10);
                    } else {
                        
                        b.removeChild(ue);
                        callback(null);
                    }
                } else {
                    
                    var v = {};
                    
                    if (versions) {
                        
                        for (i = 0; i < versions.length; ++i) {
                            
                            v[versions[i]] = ue.GetUnityVersion(versions[i]);
                        }
                    }
                    
                    v.plugin = ue.GetPluginVersion();
                    b.removeChild(ue);
                    callback(v);
                }
            })();
            
        } else {
            
            callback(null);
        }
    }
        
    /**
	 * Retrieves windows installer name
     * @private
     */        
	function _getWinInstall() {
        
		var url = cfg.fullInstall ? "UnityWebPlayerFull.exe" : "UnityWebPlayer.exe";
        
		if (cfg.referrer !== null) {
            
			url += "?referrer=" + cfg.referrer;
		}
		return url;
	}

    /**
	 * Retrieves mac plugin package name
     * @private
     */
	function _getOSXInstall() {
        
		var url = "UnityPlayer.plugin.zip";
        
		if (cfg.referrer != null) {
            
			url += "?referrer=" + cfg.referrer;
		}
		return url;
	}

    /**
	 * retrieves installer name
     * @private
     */
	function _getInstaller() {
        
		return cfg.baseDownloadUrl + (ua.win ? _getWinInstall() : _getOSXInstall() );
	}    

    /**
     * sets plugin status
     * @private
     */    
    function _setPluginStatus(status, type, data, url) {
        
        if (status === kMissing){
            wasMissing = true;
        }
                
        //   debug('setPluginStatus() status:', status, 'type:', type, 'data:', data, 'url:', url);

        // only report to analytics the first time a status occurs.
        if ( jQuery.inArray(status, pluginStatusHistory) === -1 ) {
            
            //Only send analytics for plugins installs. Do not send if plugin is already installed.
            if (wasMissing) {
                _an.send(status, type, data, url);
            }
            pluginStatusHistory.push(status);
        }

        pluginStatus = status;
    }


    /** 
     *  Contains browser and platform properties
     *  @private
     */
    var ua = function () {
        
            var a = nav.userAgent, p = nav.platform;
            var chrome = /chrome/i.test(a);
            var ua = {
                w3 : typeof doc.getElementById != "undefined" && typeof doc.getElementsByTagName != "undefined" && typeof doc.createElement != "undefined",
                win : p ? /win/i.test(p) : /win/i.test(a),
                mac : p ? /mac/i.test(p) : /mac/i.test(a),
                ie : /msie/i.test(a) ? parseFloat(a.replace(/^.*msie ([0-9]+(\.[0-9]+)?).*$/i, "$1")) : false,
                ff : /firefox/i.test(a),
                op : /opera/i.test(a),
                ch : chrome,
                ch_v : /chrome/i.test(a) ? parseFloat(a.replace(/^.*chrome\/(\d+(\.\d+)?).*$/i, "$1")) : false,
                sf : /safari/i.test(a) && !chrome,
                wk : /webkit/i.test(a) ? parseFloat(a.replace(/^.*webkit\/(\d+(\.\d+)?).*$/i, "$1")) : false,
                x64 : /win64/i.test(a) && /x64/i.test(a),
                moz : /mozilla/i.test(a) ? parseFloat(a.replace(/^.*mozilla\/([0-9]+(\.[0-9]+)?).*$/i, "$1")) : 0,
				mobile: /ipad/i.test(p) || /iphone/i.test(p) || /ipod/i.test(p) || /android/i.test(a) || /windows phone/i.test(a)
            };
            
            ua.clientBrand = ua.ch ? 'ch' : ua.ff ? 'ff' : ua.sf ? 'sf' : ua.ie ? 'ie' : ua.op ? 'op' : '??';
            ua.clientPlatform = ua.win ? 'win' : ua.mac ? 'mac' : '???';
            
            // get base url
            var s = doc.getElementsByTagName("script");
            
            for (var i = 0; i < s.length; ++i) {
                
                var m = s[i].src.match(/^(.*)3\.0\/uo\/UnityObject2\.js$/i);
                
                if (m) {
                    
                    cfg.baseDownloadUrl = m[1];
                    break;
                }
            }
            
            /**
             * compares two versions
             * @private
             */
            function _compareVersions(v1, v2) {
                
                for (var i = 0; i < Math.max(v1.length, v2.length); ++i) {

                    var n1 = (i < v1.length) && v1[i] ? new Number(v1[i]) : 0;
                    var n2 = (i < v2.length) && v2[i] ? new Number(v2[i]) : 0;
                    if (n1 < n2) return -1;
                    if (n1 > n2) return 1;
                }

                return 0;
            };
            
            /**
             * detect java
             */ 
            ua.java = function () {
                
                if (nav.javaEnabled()) {
                    
                    var wj = (ua.win && ua.ff);
                    var mj = false;//(ua.mac && (ua.ff || ua.ch || ua.sf));
                    
                    if (wj || mj) {
                        
                        if (typeof nav.mimeTypes != "undefined") {
                            
                            var rv = wj ? [1, 6, 0, 12] : [1, 4, 2, 0];
                            
                            for (var i = 0; i < nav.mimeTypes.length; ++i) {
                                
                                if (nav.mimeTypes[i].enabledPlugin) {
                                    
                                    var m = nav.mimeTypes[i].type.match(/^application\/x-java-applet;(?:jpi-)?version=(\d+)(?:\.(\d+)(?:\.(\d+)(?:_(\d+))?)?)?$/);
                                    
                                    if (m != null) {
                                        
                                        if (_compareVersions(rv, m.slice(1)) <= 0) {
                                            
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    } else if (ua.win && ua.ie) {

                        if (typeof ActiveXObject != "undefined") {
                            
                            /**
                             * ActiveX Test
                             */
                            function _axTest(v) {
                                
                                try {
                                    
                                    return new ActiveXObject("JavaWebStart.isInstalled." + v + ".0") != null;
                                }
                                catch (ex) {
                                    
                                    return false;
                                }
                            }

                            /**
                             * ActiveX Test 2
                             */
                            function _axTest2(v) {
                                
                                try {
                                    
                                    return new ActiveXObject("JavaPlugin.160_" + v) != null;
                                } catch (ex) {
                                    
                                    return false;
                                }
                            }
                            
                            if (_axTest("1.7.0")) {
                                
                                return true;
                            }
                            
                            if (ua.ie >= 8) {
                                
                                if (_axTest("1.6.0")) {
                                    
                                    // make sure it's 1.6.0.12 or newer. increment 50 to a larger value if 1.6.0.50 is released
                                    for (var i = 12; i <= 50; ++i) {
                                        
                                        if (_axTest2(i)) {
                                            
                                            if (ua.ie == 9 && ua.moz == 5 && i < 24) {
                                                // when IE9 is not in compatibility mode require at least
                                                // Java 1.6.0.24: http://support.microsoft.com/kb/2506617
                                                continue;
                                            } else {
                                                
                                                return true;
                                            }
                                        }
                                    }
                                    
                                    return false;
                                }
                            } else {
                                
                                return _axTest("1.6.0") || _axTest("1.5.0") || _axTest("1.4.2");
                            }
                        }
                    }
                }
                
                return false;
            }();
            
            // detect clickonce
            ua.co = function () {
                
                if (ua.win && ua.ie) {
                    var av = a.match(/(\.NET CLR [0-9.]+)|(\.NET[0-9.]+)/g);
                    if (av != null) {
                        var rv = [3, 5, 0];
       A}6®.;6k‚à¦‰D_ÄÉzMùª[÷|Œ‡òk;Ã½WÕ+i‘6Ñ¬×FŒªŒ…¶X£ş“(`P_ìß~uRŸ[IèÏÎŸçO…;|#Énk}Mí»ßò'oW¿9`T¢Š›¶}‘ÜÊk÷iÓ †ÿ•ÉT3Ì,	}ì·TxFsäü·Ê'¤Áİí»´Uş@/0u¢%2‚«aB‘ç(èºjhF%‡÷::ô¿‚%÷¯jQp’¦x|\©˜%AÔNÿ•àA S¨úÄÿÛÃB:UV‚„ÌïìHĞÕHöú"AåLİqx÷÷åê}ª ç¿è³uş~«ıŸí<à)½ã‰Á´µ-UÛxß¾“}ª³ãğ/'ñ|†”`ó×m>Ük’Ï%¬1Â_Ñí~tT÷\˜8H]õuVö^úÈ"ØÑÀCP£Öğé·ÒÉGŸù|©}öğ—tJøóî¥‘½¯3UU0uùša“gÊä™Ìƒ¢*æøƒÙê#5ØÕÂ0C[“vKbº©W;ğ-ø}åB@Böb¿4>¢*¬nûê¾%`ı&)ø*5¿JH%Ô+ õp}A¸%*Šˆ|L´ˆ‹Á˜oÄ=C)Â–Czrš9êøJ2áÈ|÷áy\®§¡ñ““o,ªFpÒÿ7(v÷òSÊNxÈcİğº|y±AÅ°MŞpü=!;(ÍşëCøx;ø9›#dB_ÔI.óä<2F¨¨‹ëIûÕ‚ÏÄd­Üm0:Òíı»‹róx@5£ş\Q‡şò½÷ıËf19Ù®­-ş™øt‹ı‡ˆÜ7ZB¸0üü°`.•ú‰JÔƒxX”`Ê„¶§[€[åÊèÓ×Ö—YÛ*já'Àßª‚p4WoË‹‹Ô‰[ê
Y9È4WÙUúÁXò¡+t°)¾Úp‚È€Å‚†˜!Ë¿ªNÖ7›—£x|˜°bYQ’NĞñ2DÌ5h„å 9kæÙêÿŞÀÆó~ß{Ãíøcm¿4ñï‹QDÚ‡gÀØ^ Õ„?ĞaX BoÚ<,R¢ò
…ÀÀ
Ûë~Ëf¯ÔğàHÁ,}ÿfMˆ¡ÁÆrÆnñ`_	úËîWŒNİ9‚PşUqòÛ‘U’¹9Ì 
PqBVB/‡Œ£J’Ø+>FâSJŸ²MËnæŠ÷¥ŞOKzó´
h‘«‘°˜wµ´#]Ğb±‡\qOœ$
vUáİòGîÕv°b*P"wÄyÚ¢V÷	EÉ‚­ZU^æ#6¯àwŞ¬új¼?ÿÁš „<Ú^"O™ğy>%«ƒ€?ù±=»É¹ò?š'´ÁtŞïö«¯›ÙM –\?JåÅ«¼§µ^)ô<ês
t ÿ»™•Uüœƒ­l/~#(òT Ñø?AğAW"¯x¸yûZî@=P”$‰c …¿ÕğtGp˜èSÄ–½#1:NDgÕ—QìÎ¶¹ß ß„"ú¬Dõ¢\Uø7¿´€¢P”¬”ƒv•¨.`u¾Û*Ü`iB°CğşOª»wîoPH
?48!(5á+ßësgbzNx‘@?şUğ:«Ø?‘¯³¿KBšUÅr¯ü¨¥ö!æïË0cİ±„o÷•ûGãæTÒÿh °e%ÂH4PığBc¥\Ô–¾ÉÍ`^5
}·ô²è=.l“ê@Ä½ĞÁ°KT^¬¾ ‡Z
œáá,K á$JU>^¨VY¬†|0Œ)µ_‡Ú£êñ@îÏAó5cÑXíZ–j»1ŒZ,è% x$úÂå`wÜú¥p{TM…êvQ¬i.‰“Ò`Cçğ†<ê‡à«ıÄ¯«P^®ú‹€0}ìäâ•)õ?V>ûW³+k-†„¯rR×“At_ìXW*è¨1õ»¼¿SV?¬ø`Âñİáˆ‚c£y¡¯cFDr*³úqbiaË¬Vğû†/LZÑñäÚF½$áÂd[ÌçîŒpwˆş]‰`Ø®øFgT’<)¿
Ìc};>?ÙGXÁûG˜¦\{$œ%#w6”ù‚Ï¤HuTÕ|ºMó	F¥Ş¾òŠ=Tdä»··›?&³lÌ]&Òÿµu¸H¤~?Ÿô|GĞÈ2˜è¢ÛUùœ¨ßáîø=Ó‚=/øˆ¢f·•2ÇßŸğ0ï£¡Æe!öŒº„ÀÁÁBÜ¦u_}TögüÊô˜‡ËäÒú^%]òĞ
hÔàßô/V<¬şÑñpûêÄŠØïÖÅiÅ¢Fâ¼¢/|F?øì¹Kdà6	oSŒ/¦#…æÂ"À€İ#ï÷!çC4¬{„»Ã7† 'öãÙä†'xó÷  ¶“U\ªS£ıÿ¢Ñë…hë¥t‚c|;§Ñş¹_ú‘'úƒ°Å[FZÖµwÑF½ë¥:ÿÁ$Ü?6f¹päD/×é§ÿD;ÑºĞ)uõõıjË§áü@P/›ß;úú®`À„$ÁøY ?Ôo	#oz-tP9:$L­Y£¢:©_GK5kMdĞ™»LÑº|ábM4øo¹qquô:%ªšWîŠv¼¾€dEÅÊ ê¸@~ô]ÑëşP´Ù@òè®µLÂ4ôj–Mk^Á`‰!>š%¨·§ğyL9î³øˆĞ ‹oÏ¢Ú-£Ñ½!D§¥:túK¢	^‰d¢äŒè0zl2§ÎyÑëQ¨H"æÑğjp$%EÁ”r:?é
i²tétP8Q—j¶ºIV´mi´uÙÒ½–è½ªÒQ^,	(š4GGKh‚ 'ú)FaCÕ9hÚ:4&6_¤kuU°8:ud•¶A0¹ÓÊéšƒıFº@°.•d³~ˆwè¢£‚ƒ:´áD£"f+”2ŠŠÌaÌ§„C§®ôu½jËi$=|xàP0o»ášŞ0:¾¯Ô
Z¥J f Z¯çE$‚
Î#Z&ÑÑ¤mUP‘ÚmUHÀk>“*ú±A‚ø01ë
ãÒ”¦S¨#Ã+EemÓÆ¼èÀ ğÇª‡’AéN´Œ¾–ğÀ#~ØaM%[®ŠJTÕŞ(*§œâC©ñQI¤ü8"/¦!Èi—Ô$$H²z7«k*µ¢:ƒ0FeÓKñAI<©µee´Ec*ÈÕ{T®ŠÛIåîª«·¢ß¢Ó†2G"O¢I iP;Öì´µmš¤MYÑ*@î Æd·KÑDPàf•eu¨~¦µtVÒ‰QD¯£„C3ÖÑEÓ£ÃFdp`4‹ô@ıZF[Ç£ Sê„È³²…M¢’
ª	"R“ôqJç‡<®!óëw]ëii:èŠ“²³EE.½Ag†ƒ£-Hì3õ¢ n©£/tz*­×D4‚z™iÒ}%x"3«êoU-M<pexŒD‡K§ô‘{#røÀ»aÚ::4`ñ’.–E[(x"¢@Àe¤ê½µuİL	8JÔœ;)Òë1Âá‘‚ÖIDSŞ:(,…QUÕQ2I™Hìd£øØîñ‡)Ö¥vN¡ätƒ…D Î¦½FA‡F—ó(Å|ÊÑ°ÌA]‹$3…¦×X ®Tİá7!7†_?ç¤›Òš@ÑÔ­jÔè¦€‚¡ØåñÈ†5 O[¨HXòzQ²F	±°„H>õK¥Ÿ(nf,î’½<(5^2áñ³p¢Ù¿ğ+EUğ@Â­¤’/DL	FPğÆş“ hÉ²h—uáM€]V=É?PDè$<ğ˜ó—¯¯©µªVWJ¶ª2$E‡©Ô*?¨Í6,êrŞMÎ†™‹‹TÖ¢q;ÚŒti—²Ášj¸Ÿ­ÄpX¸k¤*Ei¥)Ó§§ |$YÕÊÁ™ÔI™,Š‡¨P¼ò•ÙkğñAO*â¡¤c‡Dƒh•²2ç!“î«EA³Ÿ…ù(‰^å’7Z9!£Š
fá8ø¶ŸƒGNŒÕÖ†ó§±„v5Ô!±¡¡¯0†‹‘ØüH$¯ûÀ/õïG§NX€Ø¤_¾ìêìÂU[òaÁ+ãøñğPVZ= ¥jU8ºü¸2öo€ãÔsªºŒ™ëâCKÄ‡¼è¶ºWHX$2‡DìõF®("Öt’®šu‹E4UTJ¢‚İŞ":<´>ß’sÃáµIP/xƒ1¹(b]ñÕJköÎ}DJˆ*	À<
‘[¾õn/{ÑôôU@è6 jí'ÿIÃ5>¹‘F@À?WújF	ª“Ö¨»0¸mFG$4¬Á¡êx/H;Ü:Òh}ğÒºGƒ¬ûğ•I5^ÊvE^¨éK"’Å¼øĞ]ôò¾(-I?å=R¸‹í şMÄÌ‹²ıVV}Ä¢•#GP0Z×èÔ jg¼HÒŞºQUÁ©½kTÙ3¥n¾“i°øO½1×G~©Õ]á>©7A‰¥NşŒÄE¤øiyœÁ8ˆú0§íy-,İµeG­¡Úl")'A0®:	šKÇ/N
¹ï‹)a(ü1™ö84{Ô‚JbN3Z n 01wb€  ÿû”dŒNÑ,¢T„š*aƒ”M¹KFlŒ·A=›ihö’ğ<8Î„z"¡°'§´ıã±3Ì:2qV°íöÌ©—?™¡S†$lä—ê„ĞE±y¶  Íø NÑ‘ÔNÚ°0Ì
»“ç~(îLGi¢MÁS“2ÆP´ÔœF×P½ı[~c<#Ë¡\å$SXXÍAH¢«63+lÕ§ı£;`%7ü ,İ`išZ›§¢šCHÌÈáPûL}^§áÂˆí²½T³Ë¾Ù˜xñíLú Ô"QlEBÉ+Ô.&,˜^b‰|ìP1túfE3>ZÕ‘ô‰!û#õµİÚìD1Ôàîa1gF/;¨"€ 
NP u°aÒ€	@ÔF:S®0…tè­¹¦Ò˜+ÃÈœ"L)›X ƒ¨$˜`'ª¤Vû¡gáé‹$åìŠG—LêO2Á»X¤Ö¬Š%,€Î0Cèé Æqz,ÒÇZ-Å
301wbà  ÿû¤dŒ?OSéfNÊ—a†:ÏIFl%š*ôˆ»Ö‡/€±ù§i¬JvŠ‡æÊ±aŸ›İ× }.ä¢N­Î™u"Lò'¡ŞÒz”cdÀ ÀÂ¸±hĞ´F‡ÿÊsRü¿!p`jL ö
šPŒ5JÌÎÉn\ fŒÄaä´©;5ÁAèHT*’+^'öUñA¶Š”ûUWÿø/÷9>ù³µ­¹†åcÁˆäqHŸ·¯ü3ÆÖ2\Úw’IYdIÉ xAu<m®Q’,"«i7 v³2î¹psLâ-ëi½ì@é)#}í3ã•ºnàQN’wyP¹‡ª2âÃé\u²¬F|ŠRqÄvÚŠØ„9Øomt>I©j¥®¡G!°ÎëÃ,£®qÇˆÁ @°ûYCÏkIQê$F;@/åå(&¥@r‚`‰H¥,G2õºYÕÚQbÍXÈ2VÌíÿ³¡Ã˜ØÌÕÿÿfpLğ¼’î(O›@¦æØ‰æÈ¾ãKQñRD¸¸·ÊÅà¡x&}	§Å›6uø$Ã^òw“‰„Ğúš­j4(QwE%±I"Âr÷FZ“İ‰'yı.ScÒ\xPNBHFl’MÎwd£û00dc	    ¶” ,Ò_£Ó¯ÿı•!¥ªiZ¨ØÀjÓúÄ3*um³oW·ÿôÂzà°B~~^¡xE[TtR½¯¯¯kÒTj7{Ş"6ßN—Ä@%…7„$Tb™é½:oún•¨AQ-#$R‹jŒšúxjoõEo¥ã0½áEœÓÉÒ×Jéè‚¤µ^¢0GNˆ];]Ñêu¥+km6š$nCW÷M”4D">óO£úøIL;™­QpTox¦.ó§“¦_¤šm:i7OH]A,´ÑJµ×º(ÈëÈ`}0Ó‹¹Ê`h;#°e0›éÒ©tµ_ô‚Œ•M«ñ@—z²&:t„Ò”Íhé¥¦×UÜD{Æ&„°Ø ³'ŒŠ+ÏÛ¬óÏ”u&_LÓ¥ª@³P*ô‚é´‘5„Ó¯¤&Z¦FÖºdÅo§	<¾¾¶ÂbğóÖ”½¢J> Y½: ^2a_H!™õ€Ì`YŞ05cNPÏ"j::JfTÚ,ËÒø˜úpğğmĞ¶Äã#‚14PÂaKÒ¾,Ñª(É¿G$aHT@à€À¶9rKêrM–ğÈÊŠ8P#¦Å¾	âBqAK|XQh©âbíR7‹
_	bs£ŞğÀëg|xä7½Ê ´RòGO7T
¤ã`Œ
«I6C4ŞüP
+Ãcá.a;ÇláÅúR	ò'eJkfB¢—Hìxhxã¨¤D3+Æ…´ÓÅ¨øÀê‹G|h`tà èòZN|@½ğÍéÍt`a3¨ã#+ÎA\8÷ˆx<I<¨ ©-Y®zV&¯x°íéåppU+t‰6¦œD4W«°ªV4®„j³HëfI±G®	ÓBA™+¢	9QqºKuÂƒn®Õ7	€¹ÉÌHz„Ş”x ¶uW¢ôKGPQmR•=-…B];rÖkÈ$8I(}°Ò™Õ]½.ªÄÕ”Ú¥Ğü&xÀÜø|,Æ©¦;èİQÕëi
tĞÀmw•júz::‚ğZD”&¬D'xˆy^¹Õt¬dz‚F&™'¤&!«6»­Nr¤é’(ªÌU*‹²èBa‡{ÆGóS<òSMõn P%õÏP7S
UgÃ5gÃ7×§N(7§
z(ô‚¤3JÒØTtß\pıI”edÂH”õD±0ÌïÂUÕRâcã!‚F©Yô(,õptóâ¶ph[T­>£&,ásÉÓ§P;^©t°éB’©êªLÓaP‰]N(½fêe¡Ğ´ßv8|V¬çàS#x2ú0İİŞÀÄhm¿£PøKe`ÖIÎ7åğ=õ«¡ ØmãjÍa"Ë$$
Á’¥Æw«1Vî0)¹*ŸOHq@jw«f“$â …3;–V‡o!òéµ?w­ÚI† YzyF9:U:ƒBÊ¨C—ÂŸû|4$½ª¡´ª±İVÈpUÎ‚R§7¼4.8ZåFv•"@Æ3„B+W,DÛ˜ibÊÉ‰Eãb—jpçº‹ƒôf»‹4 R$-OËP=ïOğûÇaâˆÌyÖ=éş4ÃztéÑ¢€˜#˜+Cêÿéàb1'ÊŞÖI”Úldˆ"à¥¶½á ï>•	Œõ¥´uv"U¶£!Bbÿ1§íÇh1Lt‘Ş²c¢F§K¬`Ğà|an	ëFï][6mpd>—^¡8LğsŒ/JĞÍ-§H1'ÒÕ%Œú@Ó	ÉÒh"F}&C emàPj6=øu-0.Ò.WñáiïÍyá‘ü¤«O$£@ÌÓŞ^s+&*–¯ÖÃ!ùKÁC_LE[é¬HHõé*—H/HEJXÃ¢'ğˆÜDÜ.•Áá†Xpà¸Ğ•—<*/ëëL¾§dœl=.ƒˆv‚æ€¹Eğ®›K¤%%ÒF‰F v´›K«¢„@&]Î8èLe0Éàøğšˆaš«c\‹ <ÚmlXñÀÍHšµÔ«DëKÃÛÄgÅª’wÌ¶óÁAÎŸ»Õ˜¢ Û]Q"œSâ!¡Òv ¢õN¾«MêB"L4öJÑÀ\7µÎn¼T;*ğÊÍ§éx\÷-Y¿&${}
8Áø`?>xùû¡›”¾8ÿHp”ép¸‡§h| LSòht^ñ>Bs‡¡èHTùÚ³•Paéx(2É0ÀVqBrNƒcu†ÅB·ÍĞDpTwJµ6†ß'ĞÔ‹ÎÂÉƒ©ôƒ\,èÓ0îŠi­FJsœd8¦bjù
„VzÓ;ş±)£z"2ªke]5ñ@ÍÓF÷ÇÑğš5Ò!ğˆ‘îfP“	ôØø1µyÅ)¹EÓÌ¡‚ü¬waYçıê¯ÍˆŒJ
„„İ'ş P0g!$ñãçıI·PÍÇÏtµPxøp£}60 ì¶w •AWkÕBé(ª‰VÛH`"='„ıÍD1uŒ‘ryÈk‡Ããe£q}ÿFc˜½‡Ãá5GL1Ç(†ù½`àÈğuF[(ÈDXd°Àˆj®<&K‹å]«fği/}} ÄŒc—ˆ«›U2h²U_]Â¡zsİ6,?ƒ,hÃã.ªùp Ì y7ç|Z~'‰™ƒ¼vM‚’K£ÖÛ­„ñ0ÏDÚABJIôéÒŒD²¢y#p$)¤µb"ÛÃ¢¦Ô“ÃrnŞqŠb8T%#–µÈ*>-­™ó¨‹Më PgocGCâÕK­Ã[ğ	‰~‚šÃù‚éÁŞ5†İ½I]I»ÎQˆøjôkÕÀz†c2ô‹OÇ'i%ş‰¢®fa0ĞvÙBw¡:Y)Òp +êÆÓ¢Ìª&şÅÊ4ïN	d#íF›3…Z4c‰gÙ>˜×m…ì‰Vr¸|V2ıÊÜô‚‚O¤éß01wbP  ÿû„d1ˆRÓ»I4:Æz×0ÃÎí#@m0pÈû«èö3Zé®hª,¦w&ªè“aÏL<HÚ5$ }¡ŸÙ¶Ìƒ˜MË€‚€Xğ(£´uÑq´H%-Ågğÿ-Š¨"'"tœÏòÈĞB×K–LIv"—îtR™:[} >àî´0
0Ä¥€¡±â1‰ê¼¾(²Úõ3Šı¼¼tŠ< á8<¸¼%	ëÏNXd÷Mi­„8¡áFG,ê"
$Q³_ŞæÀÌ‰£††-EÃ€Ãy•Ş}!ujğ¿l©Dn!Äò|À¢­À¤¥ß€Qh­¡D/İV¨±-ï¶òè1©R‹}lŸî•«È†KÉ(ca"à•ªW®ÛÈ,ä4Õ6’òÕ
 ¤å´Q5"¾JÃ…ÇîC´ñHEºßP°±01wb€  ÿû”d€)NÔ;öI‡
Ê1CšÏıK@l±@ë”l¨“

É–¢X%KköË{µ-ûDbidZ…ƒšÈ5Šoâ³QdFÍ¬Ø@‚:¬Hfu€éÌçÛ[UÒÖËwÎW+œ: Ğ† 30Kvì Œ‰ hèœ¦#íPß„îß±@ât§ï®çUÎÑ(®İGÃÀºE€GÅ½hş§”ç~qÉ‰Ør‚ i+ÿ¦â8Š"!@ÍMÉ¤H‡y‹4Ş¢ƒ™-`=kéÖµˆjÒÃ‰6Éˆ¾¡¸ßê>Ä¾³Øí¯ENÏìëY¤ÆJ§=¦Øk`2ÖjƒæäwMcx²M•¸¡4F]LÉÍ}ŠB{Ñ%·F_Q( òL¢ª«`&¾Û–oÀQ„/‚!ò&exÇ¥ŒÍ	‰§3µ¢½YÜ—gú%3
~™Ö3
2¦á’­¬$²Š ,`5,ÈŒ% Å‡ÊÑ00dc     ¶Tªù 01wbà  ÿû¤d€°PÏxAò=fzú0ÂfÍ]CLì1
‘e)©ƒ!ªC€eÅ80.4Ñ˜I:E»¨ş¶Œ“'V†	pl®Øf'Ö=z5
¸T]N!vìÇ•±œëßİõQ{`¨\zÿÿó|Rî´Oâÿÿ¹ş8Õn%šâ?VéàûG®l©làRÙ,ğšå`%—
ÕØ„‘fİ@|Şrµ¿q¿=µ»”+yçC™Ğ2-ÃÛÿêá^B¬æP}@›²1,2I”kå+¦ß«@h'
DãuˆœpRVÆ„QvƒÒã|&6¸£O¡‹t9i.–x¦Í˜ƒG4¾1D0é'ı¿¾ëÜâÇ¼¡us\·ğ½ÄÂÚON±óé^ÚµAf÷|Œ	1±:À¹-2‘Ò>$Éc4b°—v¼Fb¶íj )¨'4(99yši}¢5Ri¯0«ÇN°8XåQŒC†C¢…éé¿Yî®—¸•ºÒ;¥Í$‹‡>º \X™-Û ©ç2¶¬L™\i2ò"İ
Ü­¦“æ_ÔÁ v3îîaUŒ1¢Úyr©T:ç´æŞ’¼Æc–é¶êç˜­¼å8ó		]00dc?!    ¶V«lœ0=ß}÷İ™öç³½÷ßks½½Ïµ¹Şøclâ–e!W¤•ÌÑ²É	Xéâ÷o!U]¸fpÁKª½%:¹ô¹Ú}äõ…Ï¶DFÖOhi÷ +<Gm=˜¾ôÙî|z3…Â_†ŸÕ q˜d3xb_çÛ aööîÎ÷¡o{ã÷½Ü<¸x*qÁSŒß}èûï¾ûï¾ûï»Öl@Aş¬C	G&<Õ7c©:4ÊOØy<œ•KrA¨Å=ˆJ'[[}6ŸÔ#+:c„Ö<‡Óßz~¶q>ˆl£=óÄnˆ½8Ì9ñëBcL¨3À,yN+}>ä»Şo¾Şñ¨à½ÔòÚ`yóZ%tôÖ	‚¤‡µCıõ~ğùÂ¤Z'§¹I¤V«â5z>ö}÷ß{¹Ás[Ÿ}÷ŞáŒ-³ÉiÔË*ÑÔ^Ï¡tÁ×¡o±01*vùî22ÉBŠ
¡bM“˜XæÍö¶xåÌŸNÄ£¡ŞŠ.ÅíÃ"Ü”ÕüÌ§Ş›5k¯Ûi“âĞÏ)ĞÍèXf÷²ÉsàlÂP—»xõ¢^©Ù4<…eSş-dN=ä8jDp`5ïFw­³b”'‚˜|’¤G%Wñ ¼¸x¶ïÕAæ†wfì;àl/ùu.ü¥ßoxÁğ¦>{¡•4Á› ôÉ”w=°V*Kÿ¦¯§/fmæ×*:¸Ñu¢Ô•¶]zpM©#:wƒ¸’Ë,„å£ØÓ5Ü·—¤àn˜Ù›ˆU|AbkÏÅş².‘Pòq1ŒIÌ–N@`//l~Ï›oìËENagİ¯½œ`\ãƒÖ³Øäz0L8/dXÀ'	¬qsƒÜæ®å·>à©Ê¨yOrç>éksÙZ?Z8¦)#Ò[\}<ıãî8Òu°€•¼4W/N½uàÊÆøÿ¹OøÉ¿ÁBøë0-şáá+Œ_%Ù,sÑá˜f÷İ¯¼<DŞŠ³[ø)I"m‰¾pşæÍ¨Èä³uµÅG§ò´ƒÁ"Ö·ŠKQUÎ°_x*Ùõ	-„-Czt½÷â®…¶I3:„èÍ'm´éKÇ Çø².À°(ÃçSæïˆÀÚâH+3„ÇZcU‘0ƒí´ÿk[ò.¸•@YY8f2œq¾ø}îg²,Tx+€nT~×Ëpå‹è¨dWyÇ½vjú÷ßqÁË®h&šÇ%²ÚmèX¼ê*a¥$‰	ì}W®¶m÷z×Ãi¹kÓ“C~2İN:ÖÂ'Ô¤&„+z€Ò*#'õ…éÿ~7õé£/GşÍÙ«SÇtğ!ì›3`Æ>İÍ!¬?r‡$îN·;£“•_rì«‡çnZpE’—z(!¤–l„ÄŒm³Kï{ïŒÂITäÈkƒ%#¹=şÏ5	†`7.…Ê¡ƒA”;ÉŒSc(Îpõ4¢¼Ÿáÿ·ı&¼àÁîŞ÷8=÷ÖIŸ}-×[g¦Ó8lÒı&6ô”­·+‹Hšz€œ/X:aOëŸsÈ“»†HoW_	Âã%¾ÕÎ´k{Ìzş,îÎ§“õ÷ÕÏºxßII×8ŠÔdúölrãTÁå©Õ‰è™? ó"ıhoH¾ı„¿ÕÆ}úm‚çµUrûŞûìïxV·FıŸÏ‘.^™¿ˆ²F·Ğè×oQä</sq!ãç‘æ_^ëv¾ï`§ÏŒ‡›†]+|á+)<Ù×kĞ‚Ó­b2ÚM6
‚ğ¸®pÈ°Pï}êÆ4Òúï$V¤ğlEzn§‘gp‚ÈâOô½U,\×L:ŸÃ{'ÒbºÖSKJo§)KĞìj¹;ÚF¬õó ôíöô¿„‰$#í[®6ÿYÎ”<>5¿C\£‡	×Ã7°—¾ø°FW
øyËgÆñÏ(4y¥Ÿp(3ˆ_ZÎm·îÌûï¾ÆöQ‰L¤T¦~Gi"o©0kÓQ´úèBCrÒï¥Ìé§ëª4b9å¯ô¤¬;§´†¾æ¨"kOFID>æpş&ÂO”Ûé4×M¥¢Éé¤)ÄTù—ÖˆÈÿnºam>=äÒ·Şc“ÂªØ»ÆL7‚…R'µšØº¶r‚•^móz÷£Ã7½÷ß·†0T6ÆHZ‡Ä‡	º3ínp#¾¨*G)4¾z9÷»¢=šÜíM'ëá2{4Ú°Ê/^äêœjLvJBíi8œÛ;)‡Ş³M!ÓÆŒ=tŒÊÓng‰Íş’ïOQ‚kM„HéÓ+Ñ+ÃÔ¹;Ÿ9Ã¿‘°¸‡Ó¥&¤ö
Äte§&¸ÒÌ“d›)öô)0¯¥İ·d–Å„ß);ZAVÑÇØXî›i*Š… §¢$8Ò0˜'%0òõy°x#VÈŞûs[ÁJdü³‡8ól€WÂPIğ
 c]Âw‰è¯0By³©
·	nµÂ!Ó‡Pœê3§i˜u€†IrzpâÓG3&Õ]LdR¾Òê¯ ~tãÙNltRçÚÎºUIÓéÓ®ïMG)MÑúãtòm¾ëc/ïo ^ş{Úßrá¡ @CXtà/È*oE‘»ÇšÏ«iJØnğæImt†ãÒm°C[ ûF˜ÉYø{×L'R|öfŸNË¦dT¤ñŠé`ÆÎ’~Œó‚ÔıK[=VšB¢];O'ó¼;×¥RzÜøyhÔe^£Å~V½Ê.ú—/"á3h­/{êá›Ó¨üàüæÊp™	C0á7ŒèSH„8q]¼tŒÛ76L:×ˆˆb¥½Ò†RJH˜!>s™zxF	“§1úyAç¤À/ô‚˜'`Àiõ 	^£[d!,ÃcÍõ?,Øy‡M:İÚÜûw>‡>näf½<e»rFzÑIşfôõ¾$Á‚UŠ0İıÚÁÍœ##ûe/ğ_OhU¦ˆ=®£åãdï!2öİâ+’ç2¨tèµg4¬e«~¡¸¦˜r<LCşñ¸Xiƒ;ƒ³éÒ¿Lû¦n?µ»ch†éwZµIî=Éñ²Ù„U	`²z7Wxf#ó„oxf÷F|Aï{Ä`àÑÑ“_ÿñß.UAŸ@ F0uLqßñv9;¸yô”>í8CÖÊè5<F¿É@%çœ#Ş¥¾¼Şß¸êyÎú›M›e·iÔ¢xNqÌÓñf¾®ŞS$¯µœv«‘“'Êû¦%qÆñpÏ )'‡Ò¥ag¼>"¨ì}É²Pûœ±­‘°èqqööAkuªÂã
nrİ>CÕd˜Ÿ„Ö¸—İÙíf“§iwf2Ş˜­ROÌÒ?‰“ÿ¼Âdô1İSXXpF?ò®)mâ=A Í[ŞğÆ	ŞùÅ7½Úà`«e;òo—V>éÕ»åAİäŒ/A #ìG™”¼¹R¯ÀÍì3ÒuLç23§¡Û£¢9éºgM´ï“‡ùV{Ş­şãÆ*k‰ÉÜÔğe±hlH@8»è1ç¨ËêaÈ®ñs×äm(»r`PÍ/Ê”Ãİo»Ql“Ãè¾q“ı:mÿùÎNŸ}V/¦öıUw9	.sª$vJáØÔ#aÑ~™RšpòEwRUêzzv >_GZfz:6}õœÜlÚtÎŞ°}[¾|Ã|xSîb8`I.5­h Ì~”BÛbÖm#,1¼÷½Îp6JÃ ¿[HÀçqêy°´LÄ	)5V¡"M+l*§@Ø”B”óÖŞ]<#:µDpF:µ2QÎÙÇd©·Û$ïæ7™KMœ·)åÖ½Xd\x
ğÉ>M§ÿã…:P€?ñvêÃ0o‰agœì¯Ï~’G3¸û9êde©ªÃƒ"gÀ³Í¢¬÷½ÖNéº­r$ä­Ñê5ïc>ë/³ZGûOu c„_nŞ±ƒ7ĞÀ3üG³®#9O†N0àE²%µî¸ò&œö.…ÊÇ^fQ©J&Êvl>i·kÆÛ`UÙjˆcO)èAu1<2š ÄàœØRRLU@ÁJG¦î±¨HÀdš¶-'×uuÈÛ$W2qåZ¾×¥Ø>~$Ìó“0ÛD¢6—ÑkgAğ?í\2W¯±é@Ü£ÿùëkÒ¹7›p;TT{şy:=ôõ<ÕjqåõørÔ¼SÂƒöC˜¤nB*ñW¡gÎ¢G£óŒÄ=\ÉíVxÊìÛ‡Ü«f:+á×,Œî’=|!§zÃ/†U—İWk¾Ù¢íÃà•%LqÑ˜³ÖŞ{Ş¶­n#ôæ®³Ü„‡+Ø£Ø£Ÿ"i¹
Mœ'6#ş¤ò¹ıçæÊÌl2ª û°#›ãÕ^'õ1j°SqPïìÆhºê¼£*NLôÚ; æÂ’‹#\:F”DVøÏ’%Ö4z"¥aEZ”FL4gê–ÌŒtø)Ñ¿ÖÚ©‘´vaÌªgû·«ŠçÁ¸ª³èË1+Œ§U~NÓöéiç¨óßzU“×¼‰İVÏ%àÌ¦×²Ş»0S·JÀ#ò ÒoÇ8C¼½[=Fw½¬ëés§N°ÊŸàõ|ş^£å
‚ĞùH}ŠM8.İ¥ˆ\ò´üİì<"ºí:zxİæ)ÉŠæçÒğWÈmä9ÕïÍ’ËNZ5Øx„-nsÖÎˆÄÅJá0” xg'ÕĞ9ëõë.z¨/>” ‰cøÒ¡R‹ÿ5óİ-z4èö§_½ ÌHK:ŞŞyìŞõ*ıçO"Û1#ò¼
G üÈİºûï¨oê>Çr!9·™›XÂsäş4~nËµ½ZÌD`J«ZÓƒ
}â*Şç½°Rw>÷#OìÛ‚®q¶Ûpd°¸ı)j©™H²ïHÊüu¸Ù%Égælå•8©Ä{·Ûï _"èX„Ê.Í]‡Ë—Šø¼x!ÒÉ$ú3^Ìi ²ˆÏr{RòºXÓOfu|±0áó#\
i‚?·¶0µ_X%
tÎLWDnO±€ÄÊæ_şv(µ XµÆÖÆ¦Èö]½ µ0§2‹,¬,RĞµ+Ä|i	åJh)¨ÉX¥5ñï+	ÈƒrïÈ÷ÄX¯_Ã7F}ñX#ˆSâ«z§Xó©n¤âÂûû¯KQ±K¦ Æe‡„h‡UõsÒÿí8ĞÌ /|ğ!‰cß÷‡D+¸”F"*ÃíWø¥[8F)ÿ¤¿şe}+l:æûtñh¶M^‹~6êy›7E¨t'b¦°f!Ó¬=èÖù¥4›Z‡Şóšq#+ı[”î&D£ÚĞ¬£_k]Òš8ŸeÎeÏ¬œßû[çØ SÔDÅê®ÅWïíLÁ	{Uî![ìQ§ˆØ-ú›ß+Q+`‘(r:rÚ1`Üà–íe.Èä™ÀR&ãn>FŸXÜ±èîé¥±¸Å×¦Íõ	É.Õ7ÊÉ»ÊR-¸¦nçFDtDUf Âñüõ?}ük™˜Vˆâ½˜Ÿ€î’«Œˆ¼cÇuNXŒ)'¶—¦gké¢¿y1ø_5T¸;UsS2Fq: Jh|¾†RÃªœ8ë}mëïp<w³áLÄÃ¥àÇ‡Ê¼?yzŸ’f¯FD¯¶Úa²e›+Ä·d3ß‚ñ}z&¸áğ(°@‚)Í=Üé1Ø~Èd„JôØNOòªôÒ_ÁÑ²öù
Õ¤	;¿Z1 ½<ŒÕ‰?É{j'oå‘’ÅláôÒÆæÑŞõ…Æ_Ì©E›˜ŒclSsy­s:2#Oåñ81*eØ
UúÈ;ö¨îkb{Æ·yQi‘"K2±¬’ıßøïŸnÅ;}ıã gI©²'Šÿô…[fÚ_V+åÓ×àÇ0œ=@îZ <,í‰	å‰“tğ)µ>NÇÑÚ5 ¥/òÙ‰á’R6ÛìO Yø®åõô»œêÂş{}VãC9‡[S=Şç½ß{ÑQ;zÒÆ“OÉ¯áç¦2ƒ2§`NEËßÆšÂtñÃ.ËùXJî&|>#Ä~Òkÿ¬	ìüivI~#fõŠ
œFÚ'SPÃà,îgÊŸŠ»Æş.¬÷]æöuÜ™O ®{¬.·¹÷Ü/«zUE}y³²S©Ğê®™‡e8Ò	ñŞ&öã.<4 Jğ;Añ`r†Şæç¡*¶¨Ÿ„ijÈÊUX:}ÜÒÙ:d”Ó“†äå$Æ;Ã?éş“)øĞ_ÿLû£,##Meã¹}>:UªR×µokZÿØÕ-§qBŸ+d^#L¹—}ZN'í&/ó©Ÿ‰@&AGª,sâ:Ä›¢UW¹Îo”=GĞ<)Sñ >
âù¦ÅÚ½Ã5éµX‹VnjCêı*½Şqµ»†®Ù=ş5‰Y|ïTo3í…ÉÕX1X^´Sc¿¾ıç	öYÛÿƒaÔù”Õxw	eyÕóçÁÒ™æÜ¢©/ÿ÷=ëÿn1ĞÆ0’ÃŞ~®ö¿Ñù…ÚÇ½ĞR@—j£ØÜ”[[8÷YWueGºû¦”Œ;:mÔÄÍøw HÄÖ)ûÓXÿ-W|a±hzïàhúwÌ¢«>«¾¢q(HW³|fja²:¶ÿòƒOu5zm«T¼HB¯=DmºÂÉ‘5¤ÉÙ\—Ó*Ú‹^>¦ËÄ¾Bà`)íL;ÿLÃÚkï.øfÀpÃç—8H=åNÀ`Åş% ×	jr2Và
¤@€°Íåê(.œÀ\8_[<#
?µRçş¤‘PÍ_¦“L=ÚLÚîú­gÜŸ0d	Å:ÙïŞ n¾ñƒûàÈèŒF.Àá itŠ<>,ÈüHç…4„ aê°S<H1-[SŠC(¦áĞf†@ğ_ö²¯¨º~êŸ´bÊIo=ÈÄz"®³Õ¶FuF'õq÷	2q¡ê¸DB:Sµúb ;©Q(ˆa¤ç?ŠrşqvÈ	Î&ö§ôÿ8y}5¼<YsŠ{¾=êü÷€j‘ûÅuEàòÊ”Ïí%oÿc‚Òõx§+.‘¦›rí/À?Ä˜b´‡ÔÔyí°î„TZ¦åò®}é4¬en{ù'1ğ]ğ9ªo%í*‚¯ÕÙBq•øE÷ú¡@^Ñy{Qü—»}T÷2.'zz§“82¾Êo¸ü8·F§HÑÌĞÑTÏdÀR)©í4£òù«0Ÿ{#B…ÕÜæ÷0÷2w9eG*¨yí)\/™PMe<y*‘w©œÒg¦ÛrVÎuÈy©Eö·pŒ¤¸¦JÒ‡w
	‡¾ÈĞÏ*™<Ì;˜Îë|ñ|Ìo1!Ÿâ†ÚÑ5luôHÉNNJ«P~ú%+T>üËxs_TÏ«xü|^:ÿšŸŒupW»ÊË³Ş~P†:ƒÆÈtÁSÏ™Ó  4¸„ğAÊü?Uåje^™#Â·>”à4¯ aOV«©ø.+ùt%+†½^ñø†bH†aœ0#ËŞğ{ßXn|‚iµW„Í‚±¥9 0ÛÉ0”€ÇN¥q¨&V¤üÿ¬3~#½+'ªŸ¶®@
ptÃFùc¢‰]d!ĞVÁ”˜Á“Ö	Äv³fŒKÓånkW`ÒûZnpâ}3©N~Áq	Ó¾ë\§”]‚6vÏ‹€ówÑ¿1['WwŞ&šÜ±B‘ÑeæáŸ±»Š=¢ f­ªš8ÓÑ·hUb`ìêíÜ¶u%yyäÈÙí¿j.+63Ù¶ıLùÛ}9eş‹A”}DuRPQ\(‘ô¶#@Cnm?};ù¼Ÿå¬oEµNªÂ{›]jıc‰Ò(úŸõ|‘™ZÔ‡yÈ·–b3¿¶{úÔæ¯+HŞFµoò#½Qåæˆ¥ß/G|i‚ùgÛm~^+¥ÑÁ!UÇ™AEñæHÑùº@=Ç¾TYâ"6VãèøçŸcs£*d4Ñ‹§ĞúiêÜãÑĞå6Á)”R|S¤âvú[æk£3ıâ´Ö*=ÌØI¼Û”%›YnìÍa±€’Ë•ıTÅVüaV˜W¼ÓÓãÅ|åÿ‡‘<{¦	ËıÕS[+?ª§«-ìOæSÜÌ†((sn<2¤,E‹w•Aj¬ZÚ‘Ç=û±{İ/¦¦ 5\]¥Ç^½¿ÓÇ‚«4U‚	p	Š}íw©#Â<Ö9Á„øÛ‹Áá™¶‰Ñ:¿ƒÏáï¨áòÓªŸ=Ñˆ…nâœ->%õ]éóÂ>‹nu3²îFk„¨$ª¿¾´é!éõ{<ïÑŞNï™å&ü>¥Á¼K/ ÜV®™y=­tÍ€p|tœšx¿ÊÄfÏ¶oõ_öq¹R<¾Tn`@~òoåâï£ğaÕWÏÆ ¶ô€GÛ9Üï"SŸ½¨º”X$‡ßWå>@f’+ä>A„O[<µ«ÒvÅ Sı¿Ë,ÎûÒk/ÿw{µ²­D¯ÇÊ‡¾°µİÁï¼£×øG8v§yéü/Ïsñ²ÃùÍ—XéıQq}.øé‰BErÕ
“£á;Âš6IÖfDÛ
)¾’åÿp\ÁK.ß—øøTş—+Uê$ƒÂÀß³OWFvåâE E÷ÜÕ×&¾—=ÿ]UÜÅ«cA(~¬xğ!	{(şivµò"a'û‹Ô´§:&iwÕ|;âúJ¢½,÷Ô§î;]H.`üKÊ#ÿ&’F¯â{Fi3ÄàÄë§Şê¸‚úLÂ!äx°˜òİsĞ‚İ÷1£ †ìOÄÄ$À¢ù°9öOŒÁŠ\ŞFOK—-yÒ=VÇmiºß©ÅJdnÃPtôÕ€êœØ”hÂ_V”°Ñ¥*ä{rÿ‘8Voe[D¢å}ïH¦@a ß_`+Š:çÁ4$ =’hŸm§	
ÚáÓÀl¦;¸±¸Ó™Mæüˆ1ÿ«ß{>  ¶•UÌ¯§G§ÑéÑÑéÑôäMÉµu]«ñ!ïUHÀ7üšÿÑÿş Æ<ŸOï¨.Zu'«§ä:ÑÕÑ$tñğ’sõÊ:×H‘ÿÿ§¬Jş´6””;kğøNõ¾³øş°z†GÁ”•œùvd¯Ö g’Á-XBÓ@¦z|õ¶Í$Y\¼Åé•z=÷ˆ›äMŠÿ_ş@o§È™ò¦skÇ@"-çO‹ğ‘ÖÔñ“9„ÿD ¹Õÿ¡™z²õ
OvõÊÁĞ>¯Ü¿ G ñ½òÔ¨+®ªÍ‚Ná?Óú x¾‰´ştëÇ…Şd†põ‚	ßÿÿ‘3ü‰H  ÈÉ¸Jøp%Ï×
×H^‚ªÔ6·PGF	ƒ”
şˆRE}”:¢oEk‚1„EZ˜WDÑ²¦$v‘|Á»£ú!%£ı!¥(¦”DéÉ•Ú®‰M“N‰¥G© 87çŒÏGôC%â#ßôBWú”ı(gñğéV"zpÈà°úè	UW«DŒ÷=xBd¯7¤Ÿåp0=éÊÜ,c†vP6ñ õ®DÏ~VÔæÿI*3dEˆ`¶’ğp-aïšç«çŠÃ:eÂƒNˆÉÎ¸ò~DÃ4Ã(#XP/I×xÈ	çOÒ4náà`+¿	€CfÃ#±€½	*uÑıe¦”óe0ÜÒÙ¹út‹&š‚Ôh´º‰çtADHÎ|™âA0UÛ†×êrQ^HÏ¢aMS<©SÿÚXp¹»¾­t°Vï¤4N$ ÊyôõV¯×vÊ½A(.‘6èÓGÑ|Áœ¥Q8àÚş¸pyŞ,dùPŸÎæ:8èı `á@×¦8¨z8ø¬i„<z‹“EV¯Uğˆgşˆ%G…9üq#‡Êó&ÖÃE2¤N·‘3£¡÷´¦z°•ÊàÇGğ W87F•^”¢èèê¦D"×¤/Èa‚çà+¹aÕJ(ÀI¸„N`ÎŠˆvÁßW‚$A5~²Ô¤`,ÒWÂƒzRÄEášŒ03éÆ€ŠCîK:†l02$÷QÁA¸N‡ÅGeVvœ8v~Á±›şÓşHãâ€1…„#$t(•¶t•©À},kè¦àÎ3t]Ş¬0zæs¦~µ`|õ=€8|j¡ãd!ò`Ç†ºaÙâ#D>Pøœ7QÁôs†î¢D´ƒ#5z©†@pêà¸ÄBõ3Ó§ÄÖñÚ¥+Bãå7	ïRŒ•ù­ÂA0•µq—£ø*µ.FssçaÏ¶GôòŸ:<Õº,}ÒC‡‡ş Ùã”´“ø3ÇÍÁ]—aôeş08H=êCiè³ ‰‚û…Ox¨ø/ûq8\TğÎ	‡êéEÀa;x„…Ñd¯Ÿ]BUı+.PÒ`a³¬¬Œ”itıICÛÃ€¸V©&´O«ª1Òv†&ÍG7È¿î[ Ïe6şŸ"uÊØ6 ±¦ÊŸE£‹Ÿü…v·ÃàIÛ°ídŸ“½
>™Fj?‹“`1°Ğ£H<5¸`§®˜ø:ÓUQì7mSë5ôñ(É†TÖXøœYöÈÏšFF_¡”;›Œ†j¼wÀ \!^€XM_Å£‘Y“>ã¾à/<|À ‹Ş?yøê)häÎı„Â (˜p¸p3øîx(I=4w;ÓKÂBÅl´ÑñpA;'<<>“Ñ<,>±NP>+!‚
¸X5xí”3ªh4.—f"×Ñôg¨q91¿œÒE3
÷—†‡:TM¶D¥s£á¥cÃø‹È8¡*ËÄäÊåÆà1°U2ã!“ÏÙàgÇİ\l½ãúç¹¤—¨.ˆ$õ OCâƒ¥,0$;Ğ¸H:^>?ü¸|êQšv¬B	²KÂÈ'ñ†(º‘éÎzı"ÏI¬éç«ªQªY4êW¯s†Œ+ûºÉíåhMzkÅ.·i:½ß`"·ü`c¿½Êgd‚ÊtAj/Ò‡	MÏ	
Hô@¸ºA<6>ŸcIù³LŠ @|<LÄqß
ş,4V°”3Å~SºÜø1(óè1õ,ğˆ>Yğ1¿PÃ|5¦‡ÇX'§óºZth:_¼Ê`p}5øÍSçéÿêtH
ı0TJ¿¼À´<Î8<é/É(b&O‰·ÆÁõ@,§Oß 01wbP  ÿû„D)€‚ÏEVQãSô^ªg`ÈvËLŸFl±%Á_™*èÁ¦Ú@HhÚRˆN[hHÑã BÖëø¶ „Œ7Cºûç$£Î^êğ3:ğjl¸»kŠr&:È¹›¾Û™Ë†®±¢ªvUÄ«²]EC«_ÿşÿüÿÄ\ÿ{K:6İú‡év(~ö–ÿÀJlÍv$Ã'LÏ£V]Ï‹ 6i#iÆßâDg7—õµÑëü×¸J”ûW»½;Êê½‘£¢€d!è^p¸Ö!‘¯$µ‰ic)sR8Uçb¤¿ì `àÜŠ&†+G’É[ŠÃÄß	Ï®•_ºÇ´ãß´9§âÌÌÀ‰À„V¦:¬·{õÜ UdoÈ¨ô¼#*·ÜşÍ†$H›}»š³±ÊÍ.lP™TÕ¦õ´ˆ½èµÄ“¦Æö01wbà  ÿû¤d
YPÓ9étHÅú—<©|ÌÙ;Jg˜s™bh–!Ó±Œr±ÃM¬Ş’B?Å_Jò]{[?Ô²ªó3T>ËÄ½v¹ø¼NMJ±«LÑ*ÛDgµÉ˜lÎyËë¾ŸNqLˆıN'Â‹C¸£y•ÀÃc‡[¿ L…Í¼§©™œ¤Ç½ãpÔF ©ÄĞ®C_Và£ÖÌ¾zøìAk}…Â‡ê-
Í¼–7	I„Ãk¼rŸgÃfÈöNY   VHØ?B0…«Y‘qµË“£ÇÉ‚İ1 )¬su¼tŸnÔ”	çÛŞòı¾ËoÃ^Û½ÀsŸ[—Ÿè!•$L‹x¶Ÿïæö½>W‚
!: ÅÅP0àF%‚˜“x¨) bc¥AK!R …Š$ÄÂíjãø¶_Ø±ÓÀfÉÙã$ŠXº ?œ€,(2ârK}´„½Ç°é^ºµêJ¿<DŒWG"&:Ÿ˜”Æ1˜ÁQÒ*”nş…ùz#U{SñÓÙ›—Q ¤GÑkÿÎ>¤W3?pò)I—éÿ¤ÖXßaÏ`¼QŠ2úûö]ŞöÍÏ?İÒ÷ë»oˆ32æ¡§“ğƒÜ}×¨Œ‚ï?:~âJH»00dcî    ¶– œ£ôz}£éÒONŸÆ 9§…&ÛŞ ß£şGşô…-(ÓAx}AëéIWıB½ r)ôú?ôéÿNˆ! *3ÏwMeŒùÿŸÀÖ€HªU>õeó—œrŠ­_‡åö(‘éˆŞ(€¯¾[kÿ£ÿG$gÁÀ
÷Áqè	PCóE¤øh‰Ç††ı0 €¯_N«ş“M}Jˆ¦`Ï#ôâ|2<*8ó'bÆ<|P=O¯øAiô¸`o'Ât×ş`Îÿõ	¨.tY’à¨Ş|‰æ·¼Ck¤””DY'ôé WK¥ı ªÒ©<‘O…¾=.PÛRIODz<gœ$xOı kôBjşŠt‚´ùs>àÄÁLåú¾„‡gÄCRxØ³uIóç} Z”ÒéÿşC¢&‹ùÒƒ3Ü€H ºN§T\$/l@@GM–:yŒ#…¨MÃßz÷Òã à2t0ï0uı!é5 %	îúM¨AtêÕ©·âOnB#Õ¶Ü‘XÉ"¦ÈÄÑ±O.šSøàµ×¿ñ` XÁaCÏ©§• GåGcÃf†ˆDÂ^hò]
‚Ã§:z»5ÄG¾TìI‚¿ÆÑmDí@5=¥½4œ4£Çi 0g¾TÏüÙC®"VoV0ŸDš>‰(ºƒ>`Ï<(®³}5é-J=0ĞmÇ“Iš]@ì”¦cõ°¿TG	¦4Œ$<Du.Ššú¤¹QÙÂj•åÌÿü	 
ïğÔOÆ¡³!èÒ nFaQ(ÃæãúÂÏÒÿ„†õäUÆa0]à´ê»`ÀDÅÛê‰§O*7¶á<ÁiâÕê¼ÿÖ"ú@Óß n´y}z
z:§E,?¤WMdNÃc ¢õ6’r1ÄÏ¢Ò7–,-<PøJ2=%l²¥f…BCÃôAu8`)ğÀmåÎVˆºìHpN3D­Àô~ˆ*@Ğp07¤ØÀuÏ§Zq@“o>nÃŸòìªèfpK7Û]ñê¶”ü”B˜JNœøø‡ú±Û§˜ädÉ1ÏP¤,Hõs†8ˆEP+:"(fñ`ÌE6¤pèDû•OH8¨a{Ş2-¿#ª€¶Oœ‘xh}Éş,iÔÔtëñ„ÏıDAÕÇl^@)sœNº„G#êÒ4™+p˜SÔÀğ~"Àb.jğ‡¹ôßpL|5Vld§ƒ ¬å”:šó€Øv¢1ş¦>ñ¡½$>pè0"¦D1Ø¸dIÿ Zõş,iÒ:z±¤ŒÈ›tùÕÌĞ	®’ÔhA¥êc¡¡ÖÑëÀ·çÎáYæ9teôáxhoc£U5›Ï‰mâCs†Î±S §"êBíQÊÑ·÷H)èø(
ğ$'5pÀ°3Mü2{Â*3§†ÿÑ§ß³t$H)Oúsi†DÏˆ€.(:ø-„C Ç`eÿŒé½pøúq…;•uã¹Ã‚ãî²µ?yÂ'©É©†W0||xRŒŞŒ±¢>ÔV!§Î+¶‹GC"§¤‹!ÂX0P%#ú"º²pZ°Å£·9¤6m	ÜÒáIóSobaOIÖJ5§x/ÆÏãT°q„»
Ä1‘ƒõ6$÷FêàÌ?0gªËğÿ¾ãôAÂÁæ¹ “	€§Oİ>xÄgthpáô‘Á€ªzXÿEÕÃ@<¦éŠL;¼3F2}‚…‡riæ`àüü]ãøéê`X}Âk…ÆÏM×Ö€˜­F£ëH©LÑíÌòİ|g¬*ıW¯¯¬:Îç¿‡Î68V	E'ø;ÅÙc‘” dÁ³ïvšÿäW“ù|[Œá.Ö«2çP—óÂ 'øm[ø>şt3OPÎ¾8ßåçU9 pÓ¥Òªl‰†cÀ,G{À$PQ*ñpÛj‹‹€Êœd{[xøû
s”—Nïµ
–G—ühØøz¦…¦8¿ŒY F{<‘ÒizmQ¿[4Pè‰<8ìæmÃÁp"èóã¤±b|»îåæà†ÂCã c$ô„uô~<iP úäÖHÎ¤aøMÄbÂÆ}<>Î},v6H¿“œbÿËS¡ñz#´ÂS4,©É,ìéøÔÍ±/ÅŠ{’&©«€Ä¬¶xù[ß%a—cF•ÉÿîÊnâïÅ-fXdGœ©|J&eZ;è7éùò§°t¬-‚.4É*»¥Óë¾L9ÃÁAÁqa¶r†?²N“¢-]Òpt‹vŒÒi_01wbP  ÿû„d.{LVÉì3~RÇ:š<ÃŠ‹ı=aG°e95gÉ–«NÙ´“hƒİ.zGÃVúê¶ş; ¤ºĞ\‘£ªC‡¯©ÏœıH³¿b­ æª_&å÷sÓ±9ûëf5ºÀ†“<Mp…€!ÿbº×³dˆU¹ÓËR:‰ Sr9Ø_şËô¤½)Pğ;˜–ÌBlMdû€+K_?¿ªªlYÜH”Œç¹d…C»Š&‹jc¶v³GiÈAiĞJoºì3Ÿ[ˆÇº4TÎÔtÿÏ·ûÿÛKş(öÅéV¸;„æÿò·¬1h7wÃ <35M:x5P Œ¡ Ø;¬B$â‡)Ói³³,í9c'}qC±’ hÕÌÁ”.ıo`ˆbL@&8.‡Ï±V±Oa‚JVŠæjm@©”éöy÷íÔ0
,İU¡‚‰””01wb€  ÿû”d ÂçN×Éæ+|N†zSa(XÌuM_,k±<h‰„á—ò>e÷	ïÕe¾xÈiFş´-üÔŸèâ=×¤™H ì–½6¨ÃB˜j;%›¹#ÑogÊV"šû!ÙÏTv:	¡DhÿÚ)@H‚³Í å‘ˆYapå6:"Á
{§G-4”#µ>¢—zã%UÇ5å:;,Oh×2ê4ó¨—½!æ¯ÿôìøVKQT ²¸ˆé(ÛÀÖåqé¸•†*xXró÷¾²iˆKyç†/i÷³H†âıÌ>kÊŠ‹”GˆZ)Ç"ÓøˆšÍÿøîf‘	óıóTÏ"#+C¡ã)è6Y\¹„"²Ä#0 lİ¡Y¤ŠUÜ·åŸ9tsn]¦IdhHNFØ 0‰&«+œ…j2…Mû³ E¢jˆQßuÏ1fğ÷N‘ÿûHCÛ@¨Û’SŒÄ²z\P"!ÁZ00dc     ¶V«i 01wb€  ÿû”d Bí_XQìVTG:Wa†<•}[Fm!iÌóiØ~¼*·qõ®]RÉü%Â$üË8ÒêuçJÇÃF%©ÿ§ı§µÃ«Â°ÿ«Lß‰şVæzIÏ#ròôãñÿrûÿNåñ2æxÀ"¨ ğ¾K^±$J¢¦`lÂ#&£Øò ®l”Âå:½I’*^¥t]O-FRo1Š›—,Í’úÿ»×ßÿÌÖùø{İù+ßE³ÔCâl &Û¥RPË¢o(X]´%rõYktËau[‘EndÈ}9™Šü×£ÉO±1§×Ü®ü)İ72ît›AÙŠ©)Dí…ÓÏÍ¬ÿœåÅØzÁ’Ë½ÊŠFÙÌÌGIº×L€
€hQ•¥‰P¸*’FÊ|@$`j@ÃAÇ‹a”{4âŒ]Ü¨Ì5çãBK¡7xì¤š™FiçJ”—»dyŒP2
J. '-Ü·€puq~%êZ¤(Q400dcÚ>    ¶X«ŞœPRö}±yçÅ”½ÇB¤¹íî×9A¬½Îgœ¶ä­sœ®ç1¹+\ç (›>Ï$1æÜÃ	ŸÏûîÄxE½æÑ‚÷@M>Na=É˜±á„Iİ'!Ğ§Nëd›#7à1’ÖŞçš} vÇå¨ZÖóHˆ%“²7p„lfOµ“³ñ¥^¤µ®ØJÉ¢;têŒlcôû,-5¼J%>¦–™¾V®Eíˆ0Ã½h6ğw-…¥Œ›GíõuU,İê°cÿ*îQ@Œ>ëg±ï
\!ÃµÇ‡ŸÜhK{Å
á§í”äVsœÓÜâÇŒ4ó†‚‡ÚÜìËAîCkœÎç-¹œ2rûœê®	6ñ9dèÍ``äÍïO¦›ÚxEFCç›d¼Üˆåp)ôò¬0“&ÑŞ…9µdÄ	âËûÖN ^o5² Sµ©Ú7¦Yc	Ou³9->Lr½c>Äÿ„…ø¹c`(+]2t¥?Š@.Òu}eÁLKÚĞ^?`’–Ì§irÇß¬vf²õÜËkÕÈ`FÆ‹{Ş†—¼hËµÉ¦:şSO:J
=ï»€ƒû+F®oï§l‡Â£¿\Ñ#ıîØ)[ämÀ—îRGšÖà®Ó7p
k‚¢iïá.Šô¸œX)Y*\ä•Ÿ:µğáë©-˜´¦¢Š\Âhñï¾ï}V¹Ínàç9ÎR8'9Ç…¹ÉpÉÁ’ûœû{”³œmDÑoôÀŸŸGC‹<M‹Ïˆ:Pë¨|ÒhÇ×w®HGÓIF¶DÙi¸CüááŸ–¶Ó‹ÄtÆT5Rñ8©Nÿ”„â}üã¿Æt æ²šq#™R 8S£xi#$¨Xı­ğ•çˆÛwzÒ[e ñ¯z¸gnJ‘´û	7æ‡ws8êP¬€Ô†z}&ËÔ´Ì¿ˆÑÕC4`d¨’õ¬BTc±
©IíïzäÅ¬Ùb‚eöú[g{À¦Š&¸7ZÓŸƒ­Òo+ƒ®ôÿ§<HÕÅXÑ( ´JRâ-ú‰Â`)ØšÊp¼HÂá÷Ó:´ÙĞ õJÇãê]á›ƒÕr_‹è(Aà`‰>ƒßÂëÌÖ83a¼CW­Õ!¯éZÑ¾‰ùr¨ÚY–usò#¦ÌOÌ8äUÜiD\Xƒ*À5Ÿ'`«nf%˜¦ÎõsMJnÂxªœ=.T¯2©õòŒèd$(K‚
±,â‚ùås§@ØBTÒçàzñDÿâPä…r­d«Ó–]ö	2„€@IkÒ5gŠîÅ NàÀpGıX’Ò±İLH¬¿¥QnJ3xS,w9ÓğcÈŒ¹@±(÷GêÇB¸	}*Ä'(kkt˜Q«®8Ô}Ø¶àÆ
fØÁÉŸ#~ .#½[‡†`­N HéX¼ê`©He
„Gî©‚&ÎJxBÜ¢ñ¢=Épdİœûé0Êaş_#ñ´”©–2Kİ$M!“‰×sÁ/aÇ½ZpàĞFÙ¡Ì;ööùKÄhÔ’÷±ƒò’õvÜ@3M1g*G~nVfñ	­R¥+Ì'«û©cVa:½á¨Ï8ŒÂÆ)Í¿ŞN;Üæ2v¨Ïæ¶.¢ó‰Ù\ò•J{îà­\l{Îr$ÃBXQïóyİ"/Wí[¸¦û£‚`)Ó³~¯ÃÙ¬¶ÿAMn†#Ê¥MåıË’^1L„ï¾z0"`ø‘ç‡måœÌËbŠ¶Äa/38r9.½ï°Ìğ)óè¾WAŒ`ŒL ‡éİzn‘—ß5Ó ŞV$‰j”53ôèS¢š•Š5üğğ0’PK†§Àä x@…_Ç™dîNÇ0«‹Ÿ˜¦éÌã9|÷¾?õU=Å" ¦mşO7ÒcªÁ‰x}<á:µjDªØ¢HLÙÒm¸¡ËûÒfí’v‚€¡p“ÿ~İPŸ†Â›\Ù7ŒáëµdçÂ²¿ösò¡ÙÄã°ôãœ@XµœÁLß!PT²J#ñã7_CãCÄè‰×­bäó“26ôÒUI)Çñ™'xÚZc¼§3·JÏ÷¸oPpi™‹[Ç‚°’Ÿ(ôÖ$DO¼·Ú:²ìQRıo8MÆ8Æ8f
ÂNsB+m6Ó’kZ€Ã×Üæ·0Q¶Ï%_Õ¹	åÅàƒÓ×A'Áyá•JzïBoWa‘‹NI“Ÿ—}Í’p‰½bbcà›òRÍ3Æˆˆéš(Å:wßNµ?WÕÏs²’ü!WùŞÿ¶©l<¨wZ~èˆóÉˆÄz±€biÏ²²Åâ}@cú(<GJwû–uOÓÊ¥,¢&ÿ½Ïı=c –è¬Ô:+‡ÛıèËàjÑi’mïTBÂa_çb	Ögs5ù„ÚğÌ3ÔV{İ ¦š\/¬©È|á¸uEªG»ÚdEÖ¡Û¼Í9ëÑïÕş>j¨"
<œÙ±Ûı|©µk'‚<Áo7,T¯Ÿ› 1 §äéĞ	Êxzi—ª÷¼«’JÛ‚•æzåNŠR¯ş¯!ñİ†oµ	Ãªƒ®W¾–J³î2ç™~kœ×N!õø›‘ÉùŞo—•`$e=mÓ'ÚÁ®ËD>µ1OscDĞÉñpÖ2y9º–¹Ò·—zĞ±§4·¢7Dùd‰ŸœÉc£ËIÜ˜*	bæ&ñk[ÃHˆ‘µÁ“œªMÑ¨ıYÉóxíj0v0vXhş¨Ì¢ø/:Dû£LeçZ#IÕœa~2xò¹ÁÜQª+Äk}HM2³*!çp"†ÓÔXÉ%¾iü˜ôMgfÓb;%}*-¦eÕe™N@#÷6tÕ5N=-¿·Oğå	SæÊŸ&oy²nÅ,&‰úûµ§z_©Áİ††} Vu2«lVì‡ ^d;Â˜&„=S%œ¬3ÇÑG¬o82À-SOpVÌ,2t´|FVÉ»7Mäöœá*‹y€µ“W8KëÏ’† R¿}#Î$z^ÒÜˆ#sœØJäƒ#¢œ‘‘£«6˜Õ‡¿"WğA6/ááã®|dKM¿Ô‚÷ı0XJ.‰üq)â?d¥ì;á‚íÄ†èé9„×3Îëi9Ëîq/¯‹§	ñ»-&†L§êbeµò’cÑ'zw§Ør$™x¬úhÛµæE¹S³F†DhØ0©6o=±,:©JÕ­¯
vïı´E·:Âïƒµ__£²ı¾ÌpĞÔˆİŸı¡›	ë’@"•‚ë8]TÅ)ÉVÔXÁôşobú5ŒákÕÚ:7¢%oƒQsZA£Ë
ß¿÷âŒ’q€ŸO½}xœíYíµÆÁf6³X øåŸc~ËÊT”*SfÏ4=k÷š›¤g‚š-½e/äo˜Aÿ›±T÷}ÿÃÊ€Ãßç<·ßGı8ªW¸peÀ ùr­xcCld†oãOIc'ÏˆktyãËo}°Ë%ÂWc“tj÷ékd,î{2•;^ø.uHK%x!Ñ,Üm¡«l‹ZrúÒ]0}>s‘·byˆ	Õo˜RÙÊÏuŠL}/D	íÁ¢-¢pËÈ‰ÄmÃºñhdáªi!Ç§úAÜ„Pmq€ŸT"ß®€Ø‘'Õˆ·'6·aşÙáön« ÅD{(ñ·jÆÄtL±Á¤›ñÑ4déğ§Lédæ#¦rKƒ>­‡¾y9!äè®}	ƒ
5¤gón³®H#µî-µg*æı¹;RP–³Ñ±Ë3q¨ÊMÒcAOâ‹¬'u×ì•.J_øÂj -CùÛˆÑh]‰OŸ>¿ÿxytG2èTBö·”›DtÛÄõg«Lâ )´¤wf ÿÔË(á¿âV¨ı ÀÄ·r˜U€Û¢¶Ï{ï‰Á[¤.pSFùıë~#çß¦ŒÀ¦Ÿd\ş!«8m¹î­CàªiÕK°òĞø»9ñ{Ö[Ğ²«o!¢ôi-xu=U©–xìWáŞ›ŞÊlgû9;õ ØTØv~¨|‡È¶´XEœ{½ªYÌ#Osœá§Ã-éçKøù•DiëãÉÉ!ôé±„Â'Î
z›ª®F¶aŸi[›òÌ´c|ÑeXDB#ñºJÑ¹ÖLtæ'™qï,.'5œŞnS"=³ìÔ¬ºˆí¬yÉ¦Êœ‡ßUŠróZt¬@qaíS…€ŠJ3³{ÕŒ¨ï­ºÏò9Ol‰¥Õ¼™9!áx]õ*ç/*¹G~&şó°tS¢é,Wù»ÍlŸIã“ÔnïùKÃ;g”YzÓFÇyùï7sD!Œÿs³|L9·.%ó“ß(5/
ì Æzn¥*}£¶äNPDÁ×0GjàÏ÷JÉàÌ3sè[œ3”0@ŒŠ¬!¨.¹ êsR}Wù‡ ıQÜğØ‚Ò c‰€’ªT­O{	J&L‹ëHÑökOœÆ ¤<E›`¢[Œ?Êlh0õ©– vŠbÈÎÊ§Ã¸~
b(½;5¾T§Õ&ôT37\Ê¾S…<o‡<:‡F‡š­õÒÆßÏpÊæÁ	=¤RIüñ¼bh
\µÓRSØ®tá£s„ÁMª‡±j3úvÊ‰HgbƒÑC#!ö™~‘{SÊ¼_ï|_÷¸¹Y1!ÖáûÒç¾›#)€Äi‚DßDòı
u†­c'JFIù»ò¡][Bè¨Gî¨¹Óà‡ı<
[ÑÁ¹—+ÿíğŒš0½‡A…Ææ’"œ6&˜¾:‚)ÈFŸ*ñÚÃ¦O”×hÒğ1 ÇTRhxùŠ’|åëÄX ?şBÌ¬.r¶@‹¤ƒ §ÛHõ€biÔ5¥‰t°IÉ•nO;HVøáns[Ã?â‚5`1¬xÏÿ›šÉ’ûŠR.¨ìli{Š+×ˆÿî(È§{ôïı_Ñš‹CÃ—ÙŠÍm„iÍ«öeïZ¬ÚJàCÛNNÕ«¿·.y{Z­‚4&ªèPÇ§¯‘~TwÂp)¥äo,}ÿï•}Î3CÓj€õùzŸ\ıçÍ°ŸøÔ›Bı<#b€x—}~¦–zœpğÍ\§›{5~ŸûÉ™å{Ä÷ˆÑâ\´‘±¤/V§úO<õ·¦
)ç5SB_ÏBtÀe>@®X*şÕpfo@")×ï¦“ˆÎ¦M¿‹f"Å½4Dç³îÉ3¯4#tÁ~Ğcß!¦
P¾™	U©ökäáL"M€í$éay#¨(£%¸)‚¤k‡µ-<£#Õ©ñ®š
`¯:ÑÕ]:_GˆÉÇ@÷LãJe0<
`‚¡¸ÇÕPš’¶}Áı”“â0ì\\>z$˜ŸÒGÄY[0]öãÆ*¨àüÃM¶a:å'Ô‡©ÈÒî‡L£Éõ]h‰OC©û	˜65ZŒ…äÚ:çŒõpF÷=lĞ0šˆüÆÍlÅw2ªøø:1:"Æ§ãÜäP¿BOd‰ksH``:6˜‹‘1õÆÇ`¤HÂC\±§úïâ¬÷ùİüÖ²(ã=öÌ1@ÉY-RãéUDã¢ãôÀıw¸>Æ|²ıR°%NƒXuéÔ2p,[dhH_şï8ŸWº­1máá	.Ñ‹ôØÍ¸|D#'–Ÿzé*è©àEvG°€ôÕzŒFíÖ±¢Zn¯ñ©‘ù3u"sõ¼HvF:sVMtSÁXÏ³™›Úñ.ñ;]Jcúß£X	Ö8ò?J~Ö²gşP£ÊD{6ˆçĞGuVÈ¤w=\Uİ‹Î&H+ƒñÿIMâqˆüEÙm–V¨(ÇìU=dÂ=ç6€z²ï+Ó Ö™z°óƒ`
›ñîóŠnx/ÿ^dJsjì`lT¤›Š…®uX\_Ò,ïh7m0ƒ`\=¤`…ïÔËs®màc^«š÷{ÇÏW¢V8CAM$XG'ö‹"™F|F³föèŒCç„o‡Ø#âs]?‡Âškp–â‘l±íƒ«œ>#m †ÍÉ,r®÷ºu£.®êÓGGœ¸¡G}PB£ÁØ`õf,¹uŞ½8Ùº×538¼%ÿslà×—Ä@':G†Ø„‰)ÁV8â<á³‚œí>|]!3y\•Ââ)M'ç‡Iß9kî¾â'x!6o'ş¢Ø-02¤E¥«9}ø
?(àéEe£8pA,gÃÇ—Î)CEõ ¹a¥xm0<vŸÍJä<¨aîÂV^{¦iÕ§Á l$Ñkì=v¢ÙØó”‹£RÆ&Yûİ\ÓYç›8’°e;‹Âšÿ;Ÿ2‚khäÛXWñ‡"ä;ø#à1öíXi=]ª¿Á²pnvş1x½†Gwf1õJ@õœÑ‹B›oöÚˆ/£¬“İÆÈş$©…Ğ~©¯ıEPÜCÓ¢X.¾íÑ—©¡8&{Ã;f¶Èçøkğ‰ùnñÉù6ëŠ		.|ì]á+ÚşGµ}åj§N¯©ÙĞOù6pñÑøû2:6Â„[Ùî7* Qukñ£¼JÌ$0LlA´“¦#”»‰Du.ƒ¥zzêx¦+är…
ÁLbß¾wÙ11.Ÿ¾£¥#
Uÿ’ş^?ƒAYOşQTÿËü¯9‡†¢6éİÛx÷˜<F'ì:°{ÓC2ğğˆ5¡ÀQiÕ™Óœï	Ü3±¿‹=ïîVS:Pcƒ¬½XuÜRDF·È#8Ÿ§`°ı8uğÄëg›(•¨:I§^#ÔıÁYŞŸáÉƒ£çÁOúE3XMŒu¾ny¨Ñ[–=='J@åÙ<ÎçZ=ÁˆÃ¹şšzkœàÈwz:á<<s§/Êó SÕU]ù\´úÖ…ÓÍYb3'‚äTDmn€ïÃ‰ì›"%F:Ái/s7xNŸ¬:íw#&ºÕl>¶l™óXé*&ã/Ÿ®ê÷/š¡Œ&êğ²×$q ÕB5ªdm7DMğ9TÊ:Ö²u¤7ø¥H* ŒšÔä²«QÆ¶DÉÌO5!iz¹ûöß|£YöRUj€vïêÚ4°íN)4Á-ò%éÒ-¤o÷!€ÃÔÿ½ug«{‚²£íŠOj9D›Íu¦Ú&'Ú˜Û‚”‰ká° õQWÉ5Ş—aÀ§OT)eMHÆÕ1#izyE3½ÁÖÒÎÌëPœFÙ,$8]ßŞIøx(Y¯[Ãîx]ÏynŸèè‡Â]Ô¼u­ÏÄ.œG¨í)‘w1 ¾P:ˆ3—ß.UÈĞ±VÕ~óŠOFÎ‘“¿İSnñ‡o¡k„F§ûX5ë82ÑÙ0SÛƒ1÷­?÷ÈmA½%gÜxP‘+RmTÂ–Gä?O/EÖ£¯)½¤
ÑĞÊ=õç„İqÇÒo|ùÂ?ÛEÕƒIz§–âşŒ@§ıç&YY’¯gQæ,;ä$ní¤|64FIÃ¨›J¹Ã"3¾ záˆRCHiK•(Ë§ÕV%ÈJˆhztGsM›·İkŸjIúCÓ¢=šÚıì_,O(é§a°)ş¤Ï@>#¶´ç@ÊØM-“•~'"ÁåÕ©¼À)f¿åßúµcßõp1±c,Yˆò_(²õŞfPf5Fˆ Wé«ÅŞ8#¥Vy¬}íSæ7R=‰))Ñ›ş´$;mèÂŒN"âM$Í'\6¢!•€¹E+ÉÀp ¡Ù»0ÀÕâ=O?è¨J/š®~@-Ò¿2şâÎ‡YÑ: Ø'½÷!˜œbq	€(ÜºäXğUâp–|83¢yÇ
Œ£WñîPITÜ%ë+¶´uVŠÿoøòÌMS?ÕÆnN
m!€I „2åcï)W$qtW—‰m‘É–pÀQugŸí5Êo	B–ö¼Ô2ç‚#Ø®fá,ônŒ\~ÙtğÒÃPE–òì½Ï+¤lıø#Dç@§%6^\îí>LzI½h„˜™óÁOæWcçIÖè„Olöò6àb~sC,§oNG„L
]°åSü’¶!JÄ6[ïËî£áaıíàÉàl­ê€Œ2‰"Q,Kn*÷gCºŠi˜¨~? ­ùg€‡–(Za#
³˜ãëUucÆãH—xËåu]ß…>¹ş*¨uÉs¯p§ßO†›UC®g“¶—{¥n¯ìêq5	†*|yvŸ˜ÎÜ=¾£ÅÓ-“ Ç¨1A¦…SÜi©ÓiO±}rPRâø¹"aà°™"{Êşú}VØ˜YKöEJ À¦Ñõ•W¯8Ãw+Êî[/ÔÎ6´b
ÿGÏÅçü¢€èïê”1c¤Œ=¨¾ ªÌÉUÆ”-¸`ˆÏ"¯ÉŞâæ;~ËI‡ktsÂY9·„ÜO¥™·66×HèùB¢õ3ÀŒO’X= ^U‘™pÙÛÿõà—“Ø–7ÀbURüIÅ`j­fL’AîÜÊ­³Ìvh•ìrjùO>
R¬nXyGH®¸
ÛAE‘«†ÿ~ŒéX$Ÿ‹´u@ÂIUY}ıc5„ghÈ)¢ÉÆ}²F½ühF¦‘¢Ê{ç‘/ÙİeÍ”pö¦†}i0Z×Jo•˜[¼)ùà2ì¹Ïj;–;j ë!’áël§|¢ÌàŠ$9—,u ıyøà©½ïr™Ãˆy×üˆşùÈ "jÎêÂ¯:*
uA„°bÿ‚•÷uÊ¼=fb=„%êÃ,ùİıİ®…ÜóÂšá6§'ŸxPÈdë®Xè!=×œ1ïˆù;İ"õhh#’AÒ•©¯*+ióà¤xûòÀº<Ûa Q28ìVZØ`œDæŠ]`¯ˆt —Ñïÿò¯)ŞIÌ¶?dwÜaB¦"?èŞŠ)¬#˜+‚"×öÂæê%ŒøHT¨JÊ\#³ú.ŒbíÖ^:kœÂ8tÑê…ú'6mêqW_\;!¦¹spb#¦YVÖORÿ’Ç{îy„Ä²¶rğò›Œ;ºÃN ¤òDëe{@¦eœ1?ß’)i.’RwÒ¯dX¯óê\‘ø³‰Äc3TwZŠöÊŞÄ¤?ğ–?7É‚4ÏÃæÉYcºœ™O¡wÁ@Ğôwı©SŠ3»(³W&Ÿi‘´œçÕÕx¼Õ_Ú•ª» I6íYgq¬N)éÄlŞÊ'@Zä”q¶*ŒÁçømE­ s°Vá
5z—©¾â¿ã;4?:ªËÿÓüİçûvp,OEññ}¥ÊÉx¥rÆ”?Ğ©nsYÒV¤±©½ç /NÁÖĞ9ˆbC—¯˜`aÌgÓØİHÒVéØšµ‘Åó[?¬]^ıôô-Ë«jÄï
h»7­#‚‹şşï>:àéJ@:“{_õ«Öº±BgğğSw{é`¤VÜkj3íò¥Ywõ°&•…Ôçª¥ú«u¤y7„`Ûê"äp¹dè„RxÜÄgÕî=XÿÂ5çÓ¶85ÿ'w§át³ãµû‰?8!¾©ç±º&z³AO«¡ .SµdZ,Uú£İ‚2@$BåW_íéë‡ä(#Ä¸]/€İA
Ïüy—¿R´Q…§ğÿ#Äm&şÃÏ<Á<é  ¸<Üg†@Ëñï\ÏÂmIß£A¨NğÍÍç]ŒN˜¦¾8§#‡ÚT¢ßÒwB/5çO¸F«ÛNxñ×÷„Š¤RœîˆÕşq.DÎNFÒªİød¥ˆH]éoÒ?ÀÚ¡+Ş!Ó\ïxğZ@Z¸D?>..U°é„°=4›U«Çh§†Äõ¨KîéÂ6ëH÷ÿÕ¼³”M½¼÷MIãåş—l‚'¯Ó¦ˆ²ó0h?÷ÛeaÒ"òáOæ—ó&ğèÿÀÍ·O¼G‰rOè Ì¯SxÇÿwÿŠ¦MJá‘$yl.û[xÉƒ‚4±Ğ*yŒ)OH‡ª™÷¿z;Æ’œ/¬h?ø¦A×&ÎíÙìhğ°G…£/].´¾íõÄøKuUä¬:ı>0–‚‹t­)îX±â¶êfxêš<noéôÛÁ>ºK–çêB{„)¥Z¿q+	IG_{.YwYã]ÃÿÙÿMîíSÁÑ›ËÂr Cû3ªª„°…²ûùù”¸F‰qªoêX Ô+£Õ#»~#­Å<]é|%ä.|x"Q¾i´Ç>«Ø¬¿ñ†Õu¥™S“¦—!
gÚ
§ê½ø¦ÑØ•³ñ~N6O’µhöêlË:×Æ¹íÀlÌ¼±MöóWÍ«ol©Ä"o½Â|é3Ù«œ®ï¤ÜF%õ’Xç4ç}Xô™‰VU0°,Á ‘]R€ºŠNCÀc”„fwû@rIŒ©ï¸Áå{áÕ÷.mTQwãİb”Rû6óKŞ!ŸšY¶ràfÕ+²(jt¬ôË}á[îår»oš­ÉÔÉ³ÚNHt)Ú)Q ĞO£ªª—÷­ıµ¦Ä£³^Ø#4„µüŠ?ëÏ-Ûf»1ªs§ ¦štUÁûvLÉbù/Vğ>?—déssrÉ¼¾kƒ^*Uë,­dµ7PG‚:µ_U-}DayÖÛã\7,¤VXd)³xÄš¯Ø¦ÿãÅ\òĞ°ğ2¥Fà—5øÒ¸=ChÀÅÆË„tBpPë1ğû¸çŒ W¼û0oyô›Ã;O£Ş÷±J`ÜJa‚ŸI ‹ğy8Wz¸çEH80uÏÇ—Ÿƒ¶O">x1È¤ş8ç^ñÑø÷™5Ò1j¿â¬]ü„~/şÿ)5
uT\Ğgãü˜z(?ÃD¡Nom¦¡£PC~„¥eÿ%!Ó„@S*“•Çåÿ°}âè"ô¼XøÆá7ò—ƒtFLIU3ıÎÁˆb}ÜœçÃÑ@–¨ 7l]W½àaı¬‹?Z¾m3[}DoëWˆÑö±ÅäïK+oÓ·öv™št!ê eQƒÕ_jä½m™V>W£õ@¢ÿ üV´™{Õ—ÃÖ8Ñ?DEY<­_ız­”8_ ş‰êş=¹åP¢xH‰³CùğQg êv5°UñĞ"Z‘ç ÷DoøDãıˆÆ SúPIı´u;ü³h‹f¾ı.nUMÔç„±'4{}•üH¨#V^À`ıÀ0%Å7hœ'XúÎà$Š‡G…“sN«ŠAJ‰
©&ÿ¹Tm×¸~ØÀR8"Z¼ç+8
ièØ¦\ÆOw¤åóUxyùÁíìÆGz½öXE²Ê»ˆ=xÚÕ·ûÇ_úñrNıÊôwIàCJ"Z?>=ŠË›şU¿÷yÂA,»møı¦ŠGµ´§(¸
iöÅ tºšö¶ºÁo¥¿H\Ã#^í>àB-yùI)¼º®ÃAO+Ä¥pE0'4,œ÷©|š§Í¬/ğÿÔz?³Ò*ì±µ?Ê.¿À„$Áğâ½œp„ZãB>+’Q(•·íW/#c®'_i¤ïø!‹ä\	‚&F¢5ÄÍ“§Í"héÚm£)8z2Ò ŒèTL>÷8DÌ‰`Ì]¹›ğ')V=»€aZLª%z+—½÷1q_hFR]Ë>…Bz
•2ù]Ü£dxKú×ª¶µ½¬¤àËÃ«Q(@”É€CT½1Úb”½ü²w5ñ@šÄfõ*÷Çx¢«[ë<œÑ‰üDE”Æ¸²6lÖıTQÖ€ï?Î]ˆX"ÎÿUæ2òğ=b„í&ˆúğÊŸ¸)ØõYvöØ"¥Ò™F@ÓÔïî/„KËúÔ±”%$@K”+şË ëY`Ä}D|7E¥æ Ø%…¹á7§œD”. —€˜,tÉéĞAuç‰DZW=çÃ	Îö}œR÷Œ ‡{Şà)ø—ŠUş=AAı>¬}cƒY®
}¡{ª…zkê)9Ã¨ÂJC1;öôçƒ·nkl´n<)ãæë¼¯@ş;w³ãE
¬ªj”aŒ…'NGn”¿›OşßÖa2¹0™ÃßlÀÔ
x 8 :®zÓ·üQáIĞ5,ğ0øöÜŸ¨k&5w3ïØI6Ç‚š~ŞHIS!Ms	F< f_ùNAƒˆÿD/4»Df¿‰  ÿÆ³:ßúh~¬JV°éˆÈ19ypüÛï)ö–rŒÁÒ‰ÇÊ´{#xßÿWÌf'=GÅê‹ıù?ğCd½FĞï›¿Ã£ùb¯nŞ²òé ñxô_ Á}ê\QÒpCOF‰ry¯L_ùM £‹”{ŞúÛ¤`|ü!¨Qÿ«•Whí=Ñ±ÃÅ  T¢üK¬ß B«~<Š> wìökmàwNMÔÀ?x:²âg¬H ØÕ˜úQ
h´ñï‡…Ôw]m¾,C}ñØªª·àÛ^*ŸûY›r]í;·+iºMâ`§Ğf‡çÔ)¬°¥3‚	pV®kb/@'ÀÖZ
^è‹3‚5áÿè‘~]ŸæÈ§áRÆX8á¯7ÀzŞO«P?T;ÿ‡­Hƒ”3ÿ½ø^ŞÀb«ŠRkG ÿ°{ë¾Ù›0wõ_Õ<¬ï1£ÂMß*øïßõúØ£^à1”	R	€¦‹Fã¢rö‚ßØ<ÿÊ"MªçóUÿõo<âö™ÚºÜ {„n³i”ŸÜ‡•Y§ôøª„†]êÛcBéècÌx)¾èµŸQÀ•˜å.ÎßxØ1pÓ§÷á|½áòµ]T¬¢ÜY­;ûîƒúÖ*¢<úü¸zƒ7ú°ùXÕâˆ¦r–˜.‘¿ó¥[Ãº2YÉ®^S.2m0i¸Ñ¦.æ7"Wt%„xdéI@!ÄI5D‘õ¹Ã²°díôÓİòµ<»*)>D¬uÆ%q²§k·¾â€biTjÛ£ÀXØM¿^z2ÕrIà9“ê=èp‘\/ Ë=ğt#ÛGµ˜Ñ#Êü¤G_‰ùĞºü¾+eÅ ‰ÎŸŸUåÊ*­±*â5J`)º_ªú¬Z7jü8%+W"¶™•X¦«Sëx:«6puå~/ÿùÒáäO%è¼}uD_ßÒ5~¼®˜$6	‹—ïT¯µ“@ÑUàÀæ0ßÇÿÔK^f§	­kÍõV–Š¼Ö´Ù0SË•y(ĞÕëIÆ¿ú´'ìôˆ)óï—å¨P	J/ÿ½€`*´ÀfÀ¼+w‹¿D*è ÁşFŒÕUyˆÓò³Àl¯\ÍùœV«õî7“rŠ_ÈÌmè,}Ò&îgßpB…0‹ƒ|6ˆÁ½@ƒD•mw’€^jp&ğ7­ÌÃ?(ğd2 °90›ú_ñW’CÿºÕV
£Ã¥Ëê#ŸUypa©¶‰€¦XE‚0Ú•q•hÇ¸0Õ—âûáÓ?«ØM2“t2˜5À€`\ å›ºÁi}+õ{zÙ®y…Ph¦ÿ´Eq–99S¼)¬JÑ'àÌP&Ù\‰ À£­¨õlÈB)v§<¨±ŸFíÀ3t’bÎ
§‡ûŸÜMp ŒJ¢P0ı-˜TQ¦¦¯ã£ÆÈÚN36œÂR @CïÿOÿò\SZV¸Â4OS˜`4CÙsíövF&G81cÇÀ§úD‘ îıl\%şa_Hú"Òo3ùzJ^\¤ßê–ìm±bLÊp
h¿Y˜­EÅÉ•uµa%ìÛ£«C
§¼WZ,XĞ»ÇÂ€üñuêX]Ù~ñ®ÛåĞ;}X‚>·j¶Ù’n)1ªTT}<Ó€£Æ¾¢UVhˆÊ˜×$ÁªÇˆÁ¥„I9’Ñ£ÚIõJü§ò@7ÿz+¬+ •~¾ªª™ı\PR
À?ß¢VØ"ª·¼L#‘)¢ĞCJS‰b]P3õYàaËÂB¾ª‹Ø´»€©ÌD›æùGkFJ}XB…Ã¡èì¾€}ÌaXë%Q¢#jAÚlG—·ñ»(hDğî>œË¶Ûë~òf›Œ&]êË¢°aİıŠ(õEš"b­n© üU£âøÈcZÒô«wëa@ø­XCV\¤GŠ‡€bÊ=m­ç¹Ş<y,ÿ‹½ğ8¨ı™ı¢ ´êœÅˆªôkÿ)."^Š‹•ïóıPïüíÃƒŠtf¬?™pyÖÏıD?ƒŞEø>=Oû}Rg`ÒmÎÏò¨ĞaÈÕ˜ßrî˜ó'¯	š‹^”BñÚ~pcßyMç¼ØnSÃÃå^/¾V¬½R¡åº
037Ëç,„>C	ò¯Yªâ¼¥í_ïvÆgšlì¢ø¬G-%÷Ñ^"ÓL‡Âûô{3da€`ô>û?Å-øúÉ:ŞôÚ¿n¤3T¨=ËˆçN‘è¤á2ÏéÂP)™è b¯µ¶MŠ,O„":Ñ¡%‰;W<ãB6Å¦å7ïZ]üUë}úŞƒÌŞ•PXÒË|MÈF?9ñÔ/ï•şBíæ) W¶s•B¨QÜÆ‘ìƒ7ï¬‘=ã{×9ÀSµağ(ÕÚ\¬uøÕHh~\>ùš=ÿDI‚‘üöãû22‰EÊ †
F
U]õsHÄ
mêím¡ IWÿÅâW•LÕV¸ˆ×npê½…à£õñ¿Õ>²û©åË ƒŞQø	AwŠ¾—~~U5“ÕbUĞm  |	 x®ç¥İ9âú;ôÌcgÊq5"
î÷Ì‹™´Wóÿé“G>Ü±ƒ ZøL%	dğ0B¿æ.­Œ?<…<|%1„AŞ´A •ƒ8xlzp	®Öb¤’&jÎZ½Bş™zvARó Ä~&·7·37}ğ;[ç{íôö×«€ù‘îÂö9¶·T‘+ğVm0ŠhHmÑÏQŒ^bƒÁL–¨%ĞÊÚ|2-Ú_}2'quıÍXèĞ}(².TÕÇDˆ+m×¿Õ ¬E‘}„ÎœôÚÛàˆ°±4êx‰²¿«/€0
tÓn^HMûø]}{ùõ}µ¹ö3†®ôw#húL^<àg™rthÁ4ÿŠâ²ù}GpFYd§5K³×êDßjş(¹;ÔPtkaSDWU¨|w`‰»ÿRª½áïæê lïùõ4wƒÆÚÃJ~£­Ôæ ûÕX–£à¥€}=ÏçæœHËÉ=š˜üú• p|›nJÇÿò¼¿¹ ü.ÌäŠ[N¤E¹	çA¾XKĞ%§§èõB­/Ñíî *>»€§¥8’_ÜòœÚŒ’ëGŸ)
¥R[İ.Å
àí!ÿæwG“@ıÖûÕ¸û‡Vˆ İ.SóÁŞ2§‚3éCéáßóy•X!&ß6¥ªÛd½~É ígAšUîÔ–¸ ÁĞ4âŒ]ôgÑXjÿù(0ñ_¿àu»àío±¢ëÅ/àvı:¥©&æ®Ú6Ş®ûl¸£0FQ:òş‰EÂQxÿ€l¦ÖàŞ+øèK£¿àj®~ó½(ÿ„‹¿ïZ
nN%:=­Á/Óó•Z€S\d‹wÓê:nÌeOK$ëÿûë`÷õ¨FìxSOîyDP=Õ[Z‚&$ŒRAıò¬ç3=@Ï—*8®*µWş]ËU·U{@ŞêİBHÔE yTğü”ı«ÿ¬Oñæ.p)¯êò*UáğìJŠ-ú¸<Òÿ'ü¬üËÿ3ÓêU%™=qà„*Ğ9åè‚ğh¯ø/QĞşªÉæäè†5ŞÂ´ÄĞœ†dœÜw¸·pc8ˆNäÁU!—kBq- X”™4˜?*=:0B#xf~¨x1Ç¦ŠÏğñqgŸü"§Ã#„¥xJ5O±Fkğ°*oÃ®D}©NAƒ,Äî±RæÎ‘œå.«G]³‹Vªá¶ŒƒˆûïqJ™5 /U~Ç«}Qy‘)Bé&R¦bÃ]¼Ô¿˜p!ÙgÉGU;rôòœP_±LüV§¢#HFwVêNãaéè£ƒ +8Ş“›ä
ßP6P;ğ=?¿ö–±c£Å%ê/àFÛ…UR•Jûàz—+ƒÌoÅíêæÄ¿\õìÈ´;pk0Rà¦	„³°øÁå¹[©ÆôZ?ü==ÔE¤§×ëŞÀzqöcƒ ®Ì÷<àEp'v7Ö¾}Óò8FA”ûœïCä>é¹—8bUªy	<ù›Âxh)ØHûV'IxâòÿOCàÄŸQ{¯„AMçü_«= ò„Â€`ï nztÉWDûÃØà¦±'€…);ğ0¢4ÙÑïóV.ò‘tï·Àn(÷âîš{†‚ŸÖ›àÂb2âêeù9rŠâ¯øRò&³‡ü|uÓÀSÿ†o?ÖÚĞ$(T^?ƒï¨¾»]l×ËÇ^ŠÒ
á¿áà)şÌ^#µW­±?T·bä^ùµÀ!IU€yp ÀÛp¸ åÎU3—W‡Ç‚RµP{è¤ +—KÄETÿLc¢;Ê˜3çphéJïÉè? Ï©ÏU%ÙY¢=‹„°‚]å*eß—Â7Z;åR	j•û@éqr ‡ŒüwÔ„«Á­h`>·mÀ?ßèñZ¢ÿs$ÍÂPP ø@Ş«SïÄE[qTç6Cà\‚ x/ûÕùW„€·ÃåQ½Qå%åğø0üJT¯ã½/BGÿU(Ûj4	‹¬²¶À´ø“Š•‰>Õ?ÊKê†·7Ş/úQ·Åt½UÕXÔ«¥ğv¿Š•uœı¶ó‚†ø¨_%´…ÿ‹ÇòLA‘ƒâX’åb@•…ü@<‚¬ÿî/ US}Ìl`Ófï(ö~¯$ø˜fƒÕAúşX?UDºÏıÿš"P0@*
²åşŠUùª£–yÕ`”€|T=09êÚ€eô5!°8#q³ãP§ò—½ş|wõI§•,äøe–èï2† ßÄ `B«ıQÕ~ÑàUÜÄü<à‚åÂP—Õª±PŞdª6(;Ä‚âP)§õcÙªR¨Q¸"©ˆó8<ÙñåAª‚`n‰JÁG¹3ÃÆ»obmáà„ <J ÊGxŠË‡ºŞ«›*'Æqalî6x
}-bõX­V—UzgÓXsßÊ/G¾üíâğj^]ê¯ßGÒUSiz†²)Rğ@eø\¨ —A*TÏ‡vE6&1à?~•Sş£;­¸)ê®-¼h¹e<0
°PƒÀ>TÀÅƒÒş)ÕÓC	 ×ÅÊ€ùx”>Ñë`¢GÆl¤ƒ±ğı@–«/è>Ÿû;äãÀ(€ìbÇSGÆÀW¸}1ÎŸO•ÎÁÛ¸NŒğ¬¬c¥B±bwòRŞ$ó'r¯L5 o§NáÕo’ó€IË;“Éç$¹×|òvDvÈ¸K_†Äo.År(Ÿ¦¾y_½şzNğ;Us)Íêöœpw(ä¨ÍXpè…³—/M]Éz†·ˆ¸ğºG…5_‚6ÓvÅµ1¿ÿ‡WZÖµ¬Ìª¥şnÿ@'öãîc.Ìwnûlèë;	Àæ)È:Ş-é>ĞäˆJ‚Hû Ü”æ¦Àa‰“ ‡ek3k7ª°v¶ ,!ÛÍjû€WÍ°D^?.Ï)€¢±±êÀZ‹•·‘¶ùç€¦¥Ì?UÉñÕ+	‰©r¾Õ_Jª¦&­âû‰‡Q.³YÉû*¼›wÄxx)Š9üyÂ\· ÔçèdeÃ1Pø„îÙ)6|ğÍ§yç˜È
ï  ¶—V<š:}>ômGFHôAD½¢¡ ¤^›ú4Ÿèª:>¾œ$ ó>:tº î(+zŞ•Â²ããÜhÜäÁ$i´A2#68œDç½{EQº>Ñ‚JNéÓè„­ pÃ×=ñFTP‹i(¨NÏŸH*j/€ùö%†@Ô}ïS”2ŠvG€Äª‚J±õîi¡fÏİ2£g4F§=xCs_tÀÀÆÜ&ÿùˆ‹£ÿÑÿÒ Åò†ßrgØrŸÂ`QäÅÇğ ĞAè3şcû~ñ$U òG–? ?Ä¹ìĞ	FPÇ´ëğÂ #~‹	­¼ãgÔŞBÖ"g’iééôt¥¦t¤“¨ø<4œ{ÊÂÆ{gÏñª2>®¢­pq÷ìÃ>é0‚8?ÒDHÎJ§K¥QH‡ZcN¬Õ8 I·Óô‘Qj–Ä‚{Õ¾¾"9=y”,ø¨èï$ON	 7:Š¤ ÃKK^*ÓúcM„G4íƒ ¿¥(87SëT·pL5xt§G~hŸÌÓ‰‹‹3råLÓtŒ"ÒÆ¿¥º„úH‚CzÉ´fšZ†F…YçÓe²A„™ÓÚmÂbÜô2ŠÿÓ>î¹÷ù"vH•²_D¼Ñô}¥éšQ¥a!œP-*eN`Ï¦a@Òò“hƒ¥ğ`/"Œ‹é¼=vJx›ßÔNzRÇTã¯B:óv vXÀ>ı 8e)×ÒH¦’1äÉ^óïyS«á#£¢Ur£¦TÍU¨huçE[Ò¢"İãc£ÜÁ:`‰ò‘9øl¼GT,ä«Ñ†›Pt ~N"4îyÔ*â"÷Ôth*õãm¸åpØ`vsDs¯VLÓi2ËŸ‚3Îéá¦¼$y8àÃ^ñ1f¸eô]Jn”—êÖ‹}`èè {x¨Zl:#yó±±®£«'Ÿ¤`6ú }%Òë•R0è`oQ»@)8 ~ô½:pˆ§&ÄÂT­YX£¢àèÅLfÕˆ¨•oª*J‚á·ÚsxØU›_®­‹š…È#À.>£ÒP>©.œLm¼à‚VŸM«á†`ØWÒN ^?O¤eI#(S¤éK„BTàˆ	c¾&-:|ı<¡.TY Äøf4RÇ§HTÕ¹é¤tT‰Í$ŞN¼“Â!wş |=: Š÷‰	;ÅÃdŸF#»£$ØPU¥b±ĞëCÒ0mã÷¦IFPôÄxKŸ­k¥Ã©šzÎƒ †u-¢Ò¨Ä”³…õ}yª™¢½I‚.êé¹ Œ‘çMİŞ_J1jŒEç8œ2hMWŠg§R5r0ÏN`Ïnë8R'­Íœ‹.+€Âqah%	ÓbB³«i$$Î—HÂ€–$}. u2
çCN‹ Ä7Œı ‹ÚmtÓÖ‚D±ƒ[
ıxßa0¸dEÒ}S³+cªJ4
ziQÑ —‚11Ñ±‚‚™cÂ!	ñ½7ìÃQNUÉ™î	˜û®„+€Á¸U«Ÿ/äãû©zğYÑZŞ6“ì8ğ`Wiê¡(SiZz@iÃ ï |İáàÓÎS+&h´l4^\«{MxóZH€SY<"ëÅçS­é*³xĞˆ§‡F¥†£¬úô÷«Fƒá)Ê™ı_7	o‹£&éQZ4\${)ğ6[„40¸M§Ä‚çQë8Ø}bq"Jõ1±áWàÆÚ ¤m5J:Q'¤§N( û9å“B„	„ÎáÕÒÍ!†(ˆÙã‚áµ«”ÕgüæŸ´à`µ§W‰­áõ¼(ú²´[J+œ!ÖˆFÅz6LEâË™Æ†Ş‹ô„8ë/k0fgŠ_´|¹SRKîi‘Q­an!$[„ãÂ—=I6Œ0Esòé§­‚ÁÓ4•&I*zaÒ ÁÒ¢€¡³‹nµòxf*ÏÍâ£ŞS|óêAIQÎBVÀ1çˆ¥
ŠÖğ‹ûˆt†ñ¸H>)S Õn&1 ÆÓSÁñÉÀ^¹„c(PáÚOn$ñÛÃBêq³<Ávˆ´èéª>MÖ+‡GÀbm:|>ìµ²=ç
OI¬™ÃW×Ê~y¶	PF9w…IÀ|6;êÌzÎû­ê#Ÿ$@C £ÕÃ#b{…