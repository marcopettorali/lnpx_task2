package lnpx.messages;

import java.io.Serializable;
import java.util.*;
import lnpx.Article;

public class ArticlesResponseMsg implements Serializable{

    private List<Article> articles;

    public ArticlesResponseMsg(List<Article> articles) {
        this.articles = articles;
    }

    public List<Article> getArticles() {
        return articles;
    }

}
