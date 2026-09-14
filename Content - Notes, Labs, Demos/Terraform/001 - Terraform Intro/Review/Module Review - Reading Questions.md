# Terraform Intro — Reading Questions

Use these while reading the notes. Each one has a short answer in the notes, and they're in the same order the notes cover them.

---

## 001 - What Terraform Is

1. What is infrastructure as code?

2. What three problems come with building infrastructure by hand in a console?

3. What's the difference between a declarative and a procedural approach?

4. What is a provider, and why does Terraform need one?

5. How is Terraform different from CloudFormation?

6. How do Terraform and Ansible split the work?

7. What makes Pulumi and the AWS CDK different from Terraform?

---

## 002 - The Configuration Language

8. What are the three parts of a resource block?

9. How does Terraform decide what order to build resources in?

10. What are variables for?

11. What are outputs for?

12. What is a module, and what problem does it solve?

---

## 003 - State

13. What does the state file keep track of?

14. Why does Terraform need a state file?

15. What happens when someone changes a Terraform-managed resource by hand?

16. Why doesn't a state file on one person's laptop work for a team?

17. What problem does state locking prevent?

18. Why is the state file treated as sensitive?

---

## 004 - The Workflow

19. What does `terraform init` do?

20. What does `terraform plan` show?

21. What's the difference between `plan` and `apply`?

22. When is `terraform destroy` useful?

23. Why should a reviewer read the plan, not just the code change?

24. What should we do if a plan shows a destroy we didn't expect?
