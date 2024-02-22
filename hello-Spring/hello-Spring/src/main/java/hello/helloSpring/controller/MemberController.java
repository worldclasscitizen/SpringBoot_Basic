package hello.helloSpring.controller;

import hello.helloSpring.domain.Member;
import hello.helloSpring.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class MemberController {

    /*
    * 하단 코드는 Spring 컨테이너에 등록을 하고 Spring 컨테이너로부터 받아서 쓰도록 바꿔야 한다.
    * MemberController 말고도 다른 여러 컨트롤러들이 멤버 서비스를 가져다 쓸 수 있을 것이다.
    * MemberService 에 들어가 보면 별 기능이 없다. 하나만 생성해서 공유해서 써도 되는 것이다.
    * 그렇기 때문에 new 로 매번 만들지 말고, Spring 컨테이너에 등록해서 사용하도록 하자.
    */
    // private final MemberService memberService = new MemberService();

    private final MemberService memberService;

    /*
    * @Autowired 어노테이션을 사용하면,
    * Spring 이 Spring 컨테이너에 있는 멤버 서비스를 가져다가 연결시켜준다.
    */
    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/members/new")
    public String createForm() {
        return "members/createMemberForm";
    }

    @PostMapping("/members/new")
    public String create(MemberForm form) {
        Member member = new Member();
        member.setName(form.getName());

        memberService.join(member);

        return "redirect:/";
    }

    @GetMapping("/members")
    public String list(Model model) {
        List<Member> members = memberService.findMembers();
        model.addAttribute("members", members);
        return "members/memberList";
    }

}
