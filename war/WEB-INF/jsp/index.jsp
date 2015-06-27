<%@ page import="galaxyoyo.web.neel.util.MCServerUtil.Query" contentType="text/html; charset=UTF-8" language="java" %>
<% boolean serverOnline = (boolean) request.getAttribute("server-online"); Query query = null; if (serverOnline) query = (Query) request.getAttribute("server-query"); %>
<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8" />
    <meta name="description" content="Projet de serveur Minecraft totalement RPG !" />
    <meta name="keywords" content="serveur, Minecraft, RPG, Neel, Java, galaxyoyo, DeathMagnetic, Loicgun, MechaKiwi01, 1.8, 1.8.7" />
    <meta name="robot" content="index, follow" />
    <title>Neel - Serveur Minecraft RPG</title>
  </head>
  
  <body>
    <h1>Bienvenue sur le serveur Minecraft RPG Neel !</h1>
    IP : <strong><%= request.getAttribute("server-ip") %>:<%= request.getAttribute("server-port") %></strong><br />
    <em>Cette page sera modifiée par la suite par on sait pas trop qui</em><br />
    <a href="http://www.omgserv.com/fr/contribute/235955/" target="_blank" rel="nofollow"><img src="http://www.omgserv.com/img/contribute.png"></a>
    <div style="position: absolute; top: 1em; right: 1em; text-align: justify; border: 2px solid blue; border-radius: 10px; padding: 5px;">
      <% if (serverOnline) {%>
      Statut du serveur : <span style="color: green;">ONLINE</span><br />
      Version <%= query.getVersion() %> sur <%= query.getMotor() %><br />
      <%= query.getConnectedPlayersNumber() %> joueurs connectés / <%= query.getMaxPlayers() %> :<br />
      <ul>
        <%
        for (String player : query.getConnectedPlayers())
        	out.write("<li>" + player + "</li>\r\n");
        %>
      </ul>
      <% }else{ %>
      Statut du serveur : <span style="color: green;">OFFLINE</span>
      <% } %>
    </div>
  </body>
  
  <script>
  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

  ga('create', 'UA-64293212-1', 'auto');
  ga('send', 'pageview');
  </script>
</html>