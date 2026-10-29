package com.example.demo.config;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;


@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtTokenProvider {
    @Value("${jwt.access-key}")
    @NonFinal
    String accessKey;

    @Value("${jwt.refresh-key}")
    @NonFinal
    String refreshKey;

    @Value("${jwt.access-duration}")
    @NonFinal
    long accessDuration;
    
    @Value("${jwt.refresh-duration}")
    @NonFinal
    long refreshDuration;

    private String buildToken(String userId, String key , long duration){
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        
        Instant now = Instant.now();
        Instant expiration = now.plus(duration, ChronoUnit.SECONDS);
        
        JWTClaimsSet claimsSet =
                new JWTClaimsSet.Builder()
                        .subject(userId)
                        .issuer("tipNhai")
                        .jwtID(UUID.randomUUID().toString())
                        .issueTime(Date.from(now))
                        .expirationTime(Date.from(expiration))
                        .build();

        // Dùng SignedJWT với JWTClaimsSet trực tiếp, không qua toString()
        SignedJWT signedJWT = new SignedJWT(header, claimsSet);
        try {
            signedJWT.sign(new MACSigner(key.getBytes()));
            return signedJWT.serialize();

        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    public String generateAccessToken(String userId){
        return buildToken(userId, accessKey, accessDuration);
    }

    public String generateRefreshToken(String userId){
        return buildToken(userId, refreshKey, refreshDuration);
    }

    //chỉ verify refresh token
    private SignedJWT verifyToken(String token,String key)
            throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(key.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        var verified = signedJWT.verify(verifier);
        Instant now = Instant.now();
        Date expiry =  signedJWT.getJWTClaimsSet().getExpirationTime();
        if(!(verified && expiry.after(Date.from(now)))) throw new AppException(ErrorCode.UNAUTHORIZED);
        return signedJWT;
    }

    public SignedJWT verifyAccessToken(String token) throws ParseException, JOSEException {
        return verifyToken(token,accessKey);
    }

    public SignedJWT verifyRefreshToken(String token) throws ParseException, JOSEException {
        return verifyToken(token,refreshKey);
    }

}