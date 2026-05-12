package templates;

public class OtpEmailTemplate {

    public static String buildOtpHtml(String otp) {
        return """
            <html>
                <body style="font-family:Arial; padding:20px;">
                    <h2 style="color:#4CAF50;">Your One-Time Password (OTP)</h2>
                    <p>Please use the OTP below to complete your verification.</p>

                    <div style="background:#f8f8f8; padding:15px; border-radius:8px; width:fit-content;">
                        <h1 style="letter-spacing:5px; color:#333;">%s</h1>
                    </div>

                    <p>This OTP is valid for 5 minutes. Do not share it with anyone.</p>

                    <p style="margin-top:20px;">Regards,<br/>SkyRoute Team</p>
                </body>
            </html>
        """.formatted(otp);
    }
}

