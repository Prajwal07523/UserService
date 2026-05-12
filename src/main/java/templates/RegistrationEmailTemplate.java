package templates;

public class RegistrationEmailTemplate {
	 public static String buildRegistrationHtml(String username) {

	        return """
	                <html>
	                    <body style="font-family:Arial; padding:20px;">
	                        <h2 style="color:#4CAF50;">Welcome, %s!</h2>
	                        <p>Your account has been successfully created.</p>

	                        <div style="background:#f0f0f0; padding:15px; border-radius:8px;">
	                            <p><b>Status:</b> Active ✅</p>
	                        </div>

	                        <p style="margin-top:20px;">Regards,<br/>Support Team</p>
	                    </body>
	                </html>
	                """.formatted(username);
	    }
}
