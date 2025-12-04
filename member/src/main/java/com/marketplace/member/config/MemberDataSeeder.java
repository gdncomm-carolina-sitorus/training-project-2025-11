package com.marketplace.member.config;

import com.marketplace.member.command.RegisterMemberCommand;
import com.marketplace.member.model.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("Dev")
public class MemberDataSeeder implements CommandLineRunner {
  @Autowired
  RegisterMemberCommand registerMemberCommand;

  @Override
  public void run(String... args) throws Exception {
    createMemberIfNotExist("user", "password", "user@example.com");
    createMemberIfNotExist("admin", "admin", "admin@example.com");
  }

  private void createMemberIfNotExist(String username, String password, String email) {
    try {
      RegisterRequest request = new RegisterRequest();
      request.setUsername(username);
      request.setPassword(password);
      request.setEmail(email);
      registerMemberCommand.execute(request);
      System.out.println("Seeder: Created member '" + username + "'");
    } catch (Exception e) {
      System.out.println(
          "Seeder: Member '" + username + "' already exists or failed: " + e.getMessage());
    }
  }
}
