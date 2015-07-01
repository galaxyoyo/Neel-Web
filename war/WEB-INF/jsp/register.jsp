<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="fr">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="robot" content="index, nofollow" />
    <title>Neel - Inscription</title>
  </head>
  
  <script>
	function crackUsername()
	{
		var mojangAccount = document.getElementById('mojangAccount');
		var username = document.getElementById('username');
		username.disabled = mojangAccount.checked;
		if (username.disabled)
			username.value = "";
	}
  </script>
  
  <body>
    <h1>Inscription sur le serveur</h1>
    <h3 style="color: red;">Informations importantes !</h3>
    <p>
      Ce site est directement relié à Mojang. En entrant votre adresse e-mail et votre mot de passe, cela aura le 
      même effet que de se connecter sur Minecraft, vous <strong>devez</strong> donc utiliser les mêmes identifiants.
       Si toutefois vous possédez un compte cracké, cochez la case en bas, cela aura pour effet de vérifier non plus 
      avec Mojang mais avec le serveur (/login &lt;mdp&gt;, ça vous dit quelque chose ?). Entrez donc votre pseudo, 
      envoyez votre candidature, venez sur le serveur et achetez ensuite Minecraft.
    </p>
    <form method="post">
      <label for="email">Adresse e-mail : </label>
      <input type="email" name="email" /><br />
      <label for="password">Mot de passe : </label>
      <input type="password" name="password" /><br />
      <label for="password">Confirmer : </label>
      <input type="password" name="confirm_password" /><br />
      <input type="checkbox" name="mojangAccount" id="mojangAccount" checked onchange="crackUsername();" />
      <label for="mojangAccount">Se connecter avec Mojang</label><br />
      <label for="username">Pseudo (si cracké) : </label>
      <input type="text" name="username" id="username" disabled /><br />
      <input type="submit" />
    </form>
    
    <script>
  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

  ga('create', 'UA-64293212-1', 'auto');
  ga('send', 'pageview');
  </script>
  </body>
</html>
